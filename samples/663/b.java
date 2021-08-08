import java.net.InetAddress;
import java.net.URL;
import java.net.Proxy;
import java.util.Objects;
import sun.net.www.http.HttpClient;
import sun.net.www.protocol.http.AuthenticatorKeys;
import sun.net.www.protocol.http.HttpURLConnection;
import sun.util.logging.PlatformLogger;
import static sun.net.www.protocol.http.HttpURLConnection.TunnelState.*;

class HttpsClient extends HttpClient implements HandshakeCompletedListener {
    /** See HttpClient for the model for this method. */
    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, boolean useCache, HttpURLConnection httpuc)
	    throws IOException {
	return HttpsClient.New(sf, url, hv, (String) null, -1, useCache, httpuc);
    }

    private HostnameVerifier hv;
    private SSLSocketFactory sslSocketFactory;
    private static final int httpsPortNumber = 443;

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, String proxyHost, int proxyPort,
	    boolean useCache, HttpURLConnection httpuc) throws IOException {
	return HttpsClient.New(sf, url, hv, proxyHost, proxyPort, useCache, -1, httpuc);
    }

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, String proxyHost, int proxyPort,
	    boolean useCache, int connectTimeout, HttpURLConnection httpuc) throws IOException {

	return HttpsClient.New(sf, url, hv,
		(proxyHost == null ? null : HttpClient.newHttpProxy(proxyHost, proxyPort, "https")), useCache,
		connectTimeout, httpuc);
    }

    static HttpClient New(SSLSocketFactory sf, URL url, HostnameVerifier hv, Proxy p, boolean useCache,
	    int connectTimeout, HttpURLConnection httpuc) throws IOException {
	if (p == null) {
	    p = Proxy.NO_PROXY;
	}
	PlatformLogger logger = HttpURLConnection.getHttpLogger();
	if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
	    logger.finest("Looking for HttpClient for URL " + url + " and proxy value of " + p);
	}
	HttpsClient ret = null;
	if (useCache) {
	    /* see if one's already around */
	    ret = (HttpsClient) kac.get(url, sf);
	    if (ret != null && httpuc != null && httpuc.streaming() && httpuc.getRequestMethod() == "POST") {
		if (!ret.available())
		    ret = null;
	    }

	    if (ret != null) {
		String ak = httpuc == null ? AuthenticatorKeys.DEFAULT : httpuc.getAuthenticatorKey();
		boolean compatible = ((ret.proxy != null && ret.proxy.equals(p))
			|| (ret.proxy == null && p == Proxy.NO_PROXY)) && Objects.equals(ret.getAuthenticatorKey(), ak);
		if (compatible) {
		    synchronized (ret) {
			ret.cachedHttpClient = true;
			assert ret.inCache;
			ret.inCache = false;
			if (httpuc != null && ret.needsTunneling())
			    httpuc.setTunnelState(TUNNELING);
			if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
			    logger.finest("KeepAlive stream retrieved from the cache, " + ret);
			}
		    }
		} else {
		    // We cannot return this connection to the cache as it's
		    // KeepAliveTimeout will get reset. We simply close the connection.
		    // This should be fine as it is very rare that a connection
		    // to the same host will not use the same proxy.
		    synchronized (ret) {
			if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
			    logger.finest("Not returning this connection to cache: " + ret);
			}
			ret.inCache = false;
			ret.closeServer();
		    }
		    ret = null;
		}
	    }
	}
	if (ret == null) {
	    ret = new HttpsClient(sf, url, p, connectTimeout);
	    if (httpuc != null) {
		ret.authenticatorKey = httpuc.getAuthenticatorKey();
	    }
	} else {
	    SecurityManager security = System.getSecurityManager();
	    if (security != null) {
		if (ret.proxy == Proxy.NO_PROXY || ret.proxy == null) {
		    security.checkConnect(InetAddress.getByName(url.getHost()).getHostAddress(), url.getPort());
		} else {
		    security.checkConnect(url.getHost(), url.getPort());
		}
	    }
	    ret.url = url;
	}
	ret.setHostnameVerifier(hv);

	return ret;
    }

    @Override
    public boolean needsTunneling() {
	return (proxy != null && proxy.type() != Proxy.Type.DIRECT && proxy.type() != Proxy.Type.SOCKS);
    }

    /**
     *  Same as previous constructor except using a Proxy
     */
    HttpsClient(SSLSocketFactory sf, URL url, Proxy proxy, int connectTimeout) throws IOException {
	PlatformLogger logger = HttpURLConnection.getHttpLogger();
	if (logger.isLoggable(PlatformLogger.Level.FINEST)) {
	    logger.finest("Creating new HttpsClient with url:" + url + " and proxy:" + proxy + " with connect timeout:"
		    + connectTimeout);
	}
	this.proxy = proxy;
	setSSLSocketFactory(sf);
	this.proxyDisabled = true;

	this.host = url.getHost();
	this.url = url;
	port = url.getPort();
	if (port == -1) {
	    port = getDefaultPort();
	}
	setConnectTimeout(connectTimeout);
	openServer();
    }

    void setHostnameVerifier(HostnameVerifier hv) {
	this.hv = hv;
    }

    void setSSLSocketFactory(SSLSocketFactory sf) {
	sslSocketFactory = sf;
    }

    /** Returns the default HTTPS port (443) */
    @Override
    protected int getDefaultPort() {
	return httpsPortNumber;
    }

}

