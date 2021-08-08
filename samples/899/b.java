import java.net.Socket;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.AccessController;

class NetworkClient {
    /**
     * Return a socket connected to the server, with any
     * appropriate options pre-established
     */
    protected Socket doConnect(String server, int port) throws IOException, UnknownHostException {
	Socket s;
	if (proxy != null) {
	    if (proxy.type() == Proxy.Type.SOCKS) {
		s = AccessController.doPrivileged(new PrivilegedAction&lt;&gt;() {
		    public Socket run() {
			return new Socket(proxy);
		    }
		});
	    } else if (proxy.type() == Proxy.Type.DIRECT) {
		s = createSocket();
	    } else {
		// Still connecting through a proxy
		// server & port will be the proxy address and port
		s = new Socket(Proxy.NO_PROXY);
	    }
	} else {
	    s = createSocket();
	}

	// Instance specific timeouts do have priority, that means
	// connectTimeout & readTimeout (-1 means not set)
	// Then global default timeouts
	// Then no timeout.
	if (connectTimeout &gt;= 0) {
	    s.connect(new InetSocketAddress(server, port), connectTimeout);
	} else {
	    if (defaultConnectTimeout &gt; 0) {
		s.connect(new InetSocketAddress(server, port), defaultConnectTimeout);
	    } else {
		s.connect(new InetSocketAddress(server, port));
	    }
	}
	if (readTimeout &gt;= 0)
	    s.setSoTimeout(readTimeout);
	else if (defaultSoTimeout &gt; 0) {
	    s.setSoTimeout(defaultSoTimeout);
	}
	return s;
    }

    protected Proxy proxy = Proxy.NO_PROXY;
    protected int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    protected static int defaultConnectTimeout;
    protected int readTimeout = DEFAULT_READ_TIMEOUT;
    protected static int defaultSoTimeout;

    /**
     * The following method, createSocket, is provided to allow the
     * https client to override it so that it may use its socket factory
     * to create the socket.
     */
    protected Socket createSocket() throws IOException {
	return new java.net.Socket(Proxy.NO_PROXY); // direct connection
    }

}

