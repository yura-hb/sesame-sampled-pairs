import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import java.net.http.HttpHeaders;
import jdk.internal.net.http.common.HttpHeadersBuilder;
import jdk.internal.net.http.common.Utils;

class HttpRequestImpl extends HttpRequest implements WebSocketRequest {
    /** Returns a new instance suitable for authentication. */
    public static HttpRequestImpl newInstanceForAuthentication(HttpRequestImpl other) {
	return new HttpRequestImpl(other.uri(), other.method(), other);
    }

    private final URI uri;
    private final String method;
    private final HttpHeaders userHeaders;
    private volatile boolean isWebSocket;
    private final HttpHeadersBuilder systemHeadersBuilder;
    /** The value of the User-Agent header for all requests sent by the client. */
    public static final String USER_AGENT = userAgent();
    private volatile Proxy proxy;
    final boolean expectContinue;
    final boolean secure;
    final BodyPublisher requestPublisher;
    private volatile AccessControlContext acc;
    private final Duration timeout;
    private final Optional&lt;HttpClient.Version&gt; version;
    private final InetSocketAddress authority;

    @Override
    public URI uri() {
	return uri;
    }

    /**
     * Returns the request method for this request. If not set explicitly,
     * the default method for any request is "GET".
     */
    @Override
    public String method() {
	return method;
    }

    /**
     * Creates a HttpRequestImpl using fields of an existing request impl.
     * The newly created HttpRequestImpl does not copy the system headers.
     */
    private HttpRequestImpl(URI uri, String method, HttpRequestImpl other) {
	assert method == null || Utils.isValidName(method);
	this.method = method == null ? "GET" : method;
	this.userHeaders = other.userHeaders;
	this.isWebSocket = other.isWebSocket;
	this.systemHeadersBuilder = new HttpHeadersBuilder();
	if (!userHeaders.firstValue("User-Agent").isPresent()) {
	    this.systemHeadersBuilder.setHeader("User-Agent", USER_AGENT);
	}
	this.uri = uri;
	this.proxy = other.proxy;
	this.expectContinue = other.expectContinue;
	this.secure = uri.getScheme().toLowerCase(Locale.US).equals("https");
	this.requestPublisher = other.requestPublisher; // may be null
	this.acc = other.acc;
	this.timeout = other.timeout;
	this.version = other.version();
	this.authority = null;
    }

    @Override
    public Optional&lt;HttpClient.Version&gt; version() {
	return version;
    }

}

