import java.io.*;
import java.net.*;
import java.util.Locale;
import sun.net.NetworkClient;
import sun.net.ProgressSource;
import sun.net.www.MessageHeader;
import sun.net.www.HeaderParser;
import sun.net.www.MeteredStream;
import sun.net.www.ParseUtil;
import sun.net.www.protocol.http.HttpURLConnection;
import sun.util.logging.PlatformLogger;

class HttpClient extends NetworkClient {
    /** Parse the first line of the HTTP request.  It usually looks
        something like: {@literal "HTTP/1.0 &lt;number&gt; comment\r\n"}. */

    public boolean parseHTTP(MessageHeader responses, ProgressSource pi, HttpURLConnection httpuc) throws IOException {
	/* If "HTTP/*" is found in the beginning, return true.  Let
	 * HttpURLConnection parse the mime header itself.
	 *
	 * If this isn't valid HTTP, then we don't try to parse a header
	 * out of the beginning of the response into the responses,
	 * and instead just queue up the output stream to it's very beginning.
	 * This seems most reasonable, and is what the NN browser does.
	 */

	try {
	    serverInput = serverSocket.getInputStream();
	    if (capture != null) {
		serverInput = new HttpCaptureInputStream(serverInput, capture);
	    }
	    serverInput = new BufferedInputStream(serverInput);
	    return (parseHTTPHeader(responses, pi, httpuc));
	} catch (SocketTimeoutException stex) {
	    // We don't want to retry the request when the app. sets a timeout
	    // but don't close the server if timeout while waiting for 100-continue
	    if (ignoreContinue) {
		closeServer();
	    }
	    throw stex;
	} catch (IOException e) {
	    closeServer();
	    cachedHttpClient = false;
	    if (!failedOnce && requests != null) {
		failedOnce = true;
		if (getRequestMethod().equals("CONNECT") || streaming
			|| (httpuc.getRequestMethod().equals("POST") && !retryPostProp)) {
		    // do not retry the request
		} else {
		    // try once more
		    openServer();
		    if (needsTunneling()) {
			MessageHeader origRequests = requests;
			httpuc.doTunneling();
			requests = origRequests;
		    }
		    afterConnect();
		    writeRequests(requests, poster);
		    return parseHTTP(responses, pi, httpuc);
		}
	    }
	    throw e;
	}

    }

    private HttpCapture capture = null;
    /** Response code for CONTINUE */
    private boolean ignoreContinue = true;
    protected boolean cachedHttpClient = false;
    boolean failedOnce = false;
    MessageHeader requests;
    boolean streaming;
    private static boolean retryPostProp = true;
    PosterOutputStream poster = null;
    int keepAliveConnections = -1;
    /**Idle timeout value, in milliseconds. Zero means infinity,
     * iff keepingAlive=true.
     * Unfortunately, we can't always believe this one.  If I'm connected
     * through a Netscape proxy to a server that sent me a keep-alive
     * time of 15 sec, the proxy unilaterally terminates my connection
     * after 5 sec.  So we have to hard code our effective timeout to
     * 4 sec for the case where we're using a proxy. *SIGH*
     */
    int keepAliveTimeout = 0;
    /** Url being fetched. */
    protected URL url;
    public boolean usingProxy = false;
    volatile boolean disableKeepAlive;
    private static final boolean cacheNTLMProp;
    private static final boolean cacheSPNEGOProp;
    private static final int HTTP_CONTINUE = 100;
    volatile boolean keepingAlive;
    protected String host;
    protected int port;
    private static final PlatformLogger logger = HttpURLConnection.getHttpLogger();
    private static boolean keepAliveProp = true;

    private boolean parseHTTPHeader(MessageHeader responses, ProgressSource pi, HttpURLConnection httpuc)
	    throws IOException {
	/* If "HTTP/*" is found in the beginning, return true.  Let
	 * HttpURLConnection parse the mime header itself.
	 *
	 * If this isn't valid HTTP, then we don't try to parse a header
	 * out of the beginning of the response into the responses,
	 * and instead just queue up the output stream to it's very beginning.
	 * This seems most reasonable, and is what the NN browser does.
	 */

	keepAliveConnections = -1;
	keepAliveTimeout = 0;

	boolean ret = false;
	byte[] b = new byte[8];

	try {
	    int nread = 0;
	    serverInput.mark(10);
	    while (nread &lt; 8) {
		int r = serverInput.read(b, nread, 8 - nread);
		if (r &lt; 0) {
		    break;
		}
		nread += r;
	    }
	    String keep = null;
	    String authenticate = null;
	    ret = b[0] == 'H' && b[1] == 'T' && b[2] == 'T' && b[3] == 'P' && b[4] == '/' && b[5] == '1' && b[6] == '.';
	    serverInput.reset();
	    if (ret) { // is valid HTTP - response started w/ "HTTP/1."
		responses.parseHeader(serverInput);

		// we've finished parsing http headers
		// check if there are any applicable cookies to set (in cache)
		CookieHandler cookieHandler = httpuc.getCookieHandler();
		if (cookieHandler != null) {
		    URI uri = ParseUtil.toURI(url);
		    // NOTE: That cast from Map shouldn't be necessary but
		    // a bug in javac is triggered under certain circumstances
		    // So we do put the cast in as a workaround until
		    // it is resolved.
		    if (uri != null)
			cookieHandler.put(uri, responses.getHeaders());
		}

		/* decide if we're keeping alive:
		 * This is a bit tricky.  There's a spec, but most current
		 * servers (10/1/96) that support this differ in dialects.
		 * If the server/client misunderstand each other, the
		 * protocol should fall back onto HTTP/1.0, no keep-alive.
		 */
		if (usingProxy) { // not likely a proxy will return this
		    keep = responses.findValue("Proxy-Connection");
		    authenticate = responses.findValue("Proxy-Authenticate");
		}
		if (keep == null) {
		    keep = responses.findValue("Connection");
		    authenticate = responses.findValue("WWW-Authenticate");
		}

		// 'disableKeepAlive' starts with the value false.
		// It can transition from false to true, but once true
		// it stays true.
		// If cacheNTLMProp is false, and disableKeepAlive is false,
		// then we need to examine the response headers to figure out
		// whether we are doing NTLM authentication. If we do NTLM,
		// and cacheNTLMProp is false, than we can't keep this connection
		// alive: we will switch disableKeepAlive to true.
		boolean canKeepAlive = !disableKeepAlive;
		if (canKeepAlive && (cacheNTLMProp == false || cacheSPNEGOProp == false) && authenticate != null) {
		    authenticate = authenticate.toLowerCase(Locale.US);
		    if (cacheNTLMProp == false) {
			canKeepAlive &= !authenticate.startsWith("ntlm ");
		    }
		    if (cacheSPNEGOProp == false) {
			canKeepAlive &= !authenticate.startsWith("negotiate ");
			canKeepAlive &= !authenticate.startsWith("kerberos ");
		    }
		}
		disableKeepAlive |= !canKeepAlive;

		if (keep != null && keep.toLowerCase(Locale.US).equals("keep-alive")) {
		    /* some servers, notably Apache1.1, send something like:
		     * "Keep-Alive: timeout=15, max=1" which we should respect.
		     */
		    if (disableKeepAlive) {
			keepAliveConnections = 1;
		    } else {
			HeaderParser p = new HeaderParser(responses.findValue("Keep-Alive"));
			/* default should be larger in case of proxy */
			keepAliveConnections = p.findInt("max", usingProxy ? 50 : 5);
			keepAliveTimeout = p.findInt("timeout", usingProxy ? 60 : 5);
		    }
		} else if (b[7] != '0') {
		    /*
		     * We're talking 1.1 or later. Keep persistent until
		     * the server says to close.
		     */
		    if (keep != null || disableKeepAlive) {
			/*
			 * The only Connection token we understand is close.
			 * Paranoia: if there is any Connection header then
			 * treat as non-persistent.
			 */
			keepAliveConnections = 1;
		    } else {
			keepAliveConnections = 5;
		    }
		}
	    } else if (nread != 8) {
		if (!failedOnce && requests != null) {
		    failedOnce = true;
		    if (getRequestMethod().equals("CONNECT") || streaming
			    || (httpuc.getRequestMethod().equals("POST") && !retryPostProp)) {
			// do not retry the request
		    } else {
			closeServer();
			cachedHttpClient = false;
			openServer();
			if (needsTunneling()) {
			    MessageHeader origRequests = requests;
			    httpuc.doTunneling();
			    requests = origRequests;
			}
			afterConnect();
			writeRequests(requests, poster);
			return parseHTTP(responses, pi, httpuc);
		    }
		}
		throw new SocketException("Unexpected end of file from server");
	    } else {
		// we can't vouche for what this is....
		responses.set("Content-type", "unknown/unknown");
	    }
	} catch (IOException e) {
	    throw e;
	}

	int code = -1;
	try {
	    String resp;
	    resp = responses.getValue(0);
	    /* should have no leading/trailing LWS
	     * expedite the typical case by assuming it has
	     * form "HTTP/1.x &lt;WS&gt; 2XX &lt;mumble&gt;"
	     */
	    int ind;
	    ind = resp.indexOf(' ');
	    while (resp.charAt(ind) == ' ')
		ind++;
	    code = Integer.parseInt(resp, ind, ind + 3, 10);
	} catch (Exception e) {
	}

	if (code == HTTP_CONTINUE && ignoreContinue) {
	    responses.reset();
	    return parseHTTPHeader(responses, pi, httpuc);
	}

	long cl = -1;

	/*
	 * Set things up to parse the entity body of the reply.
	 * We should be smarter about avoid pointless work when
	 * the HTTP method and response code indicate there will be
	 * no entity body to parse.
	 */
	String te = responses.findValue("Transfer-Encoding");
	if (te != null && te.equalsIgnoreCase("chunked")) {
	    serverInput = new ChunkedInputStream(serverInput, this, responses);

	    /*
	     * If keep alive not specified then close after the stream
	     * has completed.
	     */
	    if (keepAliveConnections &lt;= 1) {
		keepAliveConnections = 1;
		keepingAlive = false;
	    } else {
		keepingAlive = !disableKeepAlive;
	    }
	    failedOnce = false;
	} else {

	    /*
	     * If it's a keep alive connection then we will keep
	     * (alive if :-
	     * 1. content-length is specified, or
	     * 2. "Not-Modified" or "No-Content" responses - RFC 2616 states that
	     *    204 or 304 response must not include a message body.
	     */
	    String cls = responses.findValue("content-length");
	    if (cls != null) {
		try {
		    cl = Long.parseLong(cls);
		} catch (NumberFormatException e) {
		    cl = -1;
		}
	    }
	    String requestLine = requests.getKey(0);

	    if ((requestLine != null && (requestLine.startsWith("HEAD"))) || code == HttpURLConnection.HTTP_NOT_MODIFIED
		    || code == HttpURLConnection.HTTP_NO_CONTENT) {
		cl = 0;
	    }

	    if (keepAliveConnections &gt; 1 && (cl &gt;= 0 || code == HttpURLConnection.HTTP_NOT_MODIFIED
		    || code == HttpURLConnection.HTTP_NO_CONTENT)) {
		keepingAlive = !disableKeepAlive;
		failedOnce = false;
	    } else if (keepingAlive) {
		/* Previously we were keeping alive, and now we're not.  Remove
		 * this from the cache (but only here, once) - otherwise we get
		 * multiple removes and the cache count gets messed up.
		 */
		keepingAlive = false;
	    }
	}

	/* wrap a KeepAliveStream/MeteredStream around it if appropriate */

	if (cl &gt; 0) {
	    // In this case, content length is well known, so it is okay
	    // to wrap the input stream with KeepAliveStream/MeteredStream.

	    if (pi != null) {
		// Progress monitor is enabled
		pi.setContentType(responses.findValue("content-type"));
	    }

	    // If disableKeepAlive == true, the client will not be returned
	    // to the cache. But we still need to use a keepalive stream to
	    // allow the multi-message authentication exchange on the connection
	    boolean useKeepAliveStream = isKeepingAlive() || disableKeepAlive;
	    if (useKeepAliveStream) {
		// Wrap KeepAliveStream if keep alive is enabled.
		logFinest("KeepAlive stream used: " + url);
		serverInput = new KeepAliveStream(serverInput, pi, cl, this);
		failedOnce = false;
	    } else {
		serverInput = new MeteredStream(serverInput, pi, cl);
	    }
	} else if (cl == -1) {
	    // In this case, content length is unknown - the input
	    // stream would simply be a regular InputStream or
	    // ChunkedInputStream.

	    if (pi != null) {
		// Progress monitoring is enabled.

		pi.setContentType(responses.findValue("content-type"));

		// Wrap MeteredStream for tracking indeterministic
		// progress, even if the input stream is ChunkedInputStream.
		serverInput = new MeteredStream(serverInput, pi, cl);
	    } else {
		// Progress monitoring is disabled, and there is no
		// need to wrap an unknown length input stream.

		// ** This is an no-op **
	    }
	} else {
	    if (pi != null)
		pi.finishTracking();
	}

	return ret;
    }

    @Override
    public void closeServer() {
	try {
	    keepingAlive = false;
	    serverSocket.close();
	} catch (Exception e) {
	}
    }

    String getRequestMethod() {
	if (requests != null) {
	    String requestLine = requests.getKey(0);
	    if (requestLine != null) {
		return requestLine.split("\\s+")[0];
	    }
	}
	return "";
    }

    protected synchronized void openServer() throws IOException {

	SecurityManager security = System.getSecurityManager();

	if (security != null) {
	    security.checkConnect(host, port);
	}

	if (keepingAlive) { // already opened
	    return;
	}

	if (url.getProtocol().equals("http") || url.getProtocol().equals("https")) {

	    if ((proxy != null) && (proxy.type() == Proxy.Type.HTTP)) {
		sun.net.www.URLConnection.setProxiedHost(host);
		privilegedOpenServer((InetSocketAddress) proxy.address());
		usingProxy = true;
		return;
	    } else {
		// make direct connection
		openServer(host, port);
		usingProxy = false;
		return;
	    }

	} else {
	    /* we're opening some other kind of url, most likely an
	     * ftp url.
	     */
	    if ((proxy != null) && (proxy.type() == Proxy.Type.HTTP)) {
		sun.net.www.URLConnection.setProxiedHost(host);
		privilegedOpenServer((InetSocketAddress) proxy.address());
		usingProxy = true;
		return;
	    } else {
		// make direct connection
		super.openServer(host, port);
		usingProxy = false;
		return;
	    }
	}
    }

    public boolean needsTunneling() {
	return false;
    }

    public void afterConnect() throws IOException, UnknownHostException {
	// NO-OP. Needs to be overwritten by HttpsClient
    }

    public void writeRequests(MessageHeader head, PosterOutputStream pos) throws IOException {
	requests = head;
	requests.print(serverOutput);
	poster = pos;
	if (poster != null)
	    poster.writeTo(serverOutput);
	serverOutput.flush();
    }

    public final boolean isKeepingAlive() {
	return getHttpKeepAliveSet() && keepingAlive;
    }

    private static void logFinest(String msg) {
	if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
	    logger.finest(msg);
	}
    }

    private synchronized void privilegedOpenServer(final InetSocketAddress server) throws IOException {
	try {
	    java.security.AccessController.doPrivileged(new java.security.PrivilegedExceptionAction&lt;&gt;() {
		public Void run() throws IOException {
		    openServer(server.getHostString(), server.getPort());
		    return null;
		}
	    });
	} catch (java.security.PrivilegedActionException pae) {
	    throw (IOException) pae.getException();
	}
    }

    @Override
    public void openServer(String server, int port) throws IOException {
	serverSocket = doConnect(server, port);
	try {
	    OutputStream out = serverSocket.getOutputStream();
	    if (capture != null) {
		out = new HttpCaptureOutputStream(out, capture);
	    }
	    serverOutput = new PrintStream(new BufferedOutputStream(out), false, encoding);
	} catch (UnsupportedEncodingException e) {
	    throw new InternalError(encoding + " encoding not found", e);
	}
	serverSocket.setTcpNoDelay(true);
    }

    /**
     * @return true iff http keep alive is set (i.e. enabled).  Defaults
     *          to true if the system property http.keepAlive isn't set.
     */
    public boolean getHttpKeepAliveSet() {
	return keepAliveProp;
    }

}

