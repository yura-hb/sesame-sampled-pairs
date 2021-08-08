import java.net.*;

abstract class BaseSSLSocketImpl extends SSLSocket {
    /**
     * Enables or disables the Nagle optimization.
     * @see java.net.Socket#setTcpNoDelay
     */
    @Override
    public final void setTcpNoDelay(boolean value) throws SocketException {
	if (self == this) {
	    super.setTcpNoDelay(value);
	} else {
	    self.setTcpNoDelay(value);
	}
    }

    private final Socket self;

}

