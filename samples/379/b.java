import java.io.DataOutput;
import java.io.ObjectOutput;

class TCPEndpoint implements Endpoint {
    /**
     * Write endpoint to output stream.
     */
    public void write(ObjectOutput out) throws IOException {
	if (csf == null) {
	    out.writeByte(FORMAT_HOST_PORT);
	    out.writeUTF(host);
	    out.writeInt(port);
	} else {
	    out.writeByte(FORMAT_HOST_PORT_FACTORY);
	    out.writeUTF(host);
	    out.writeInt(port);
	    out.writeObject(csf);
	}
    }

    /** custom client socket factory (null if not custom factory) */
    private final RMIClientSocketFactory csf;
    private static final int FORMAT_HOST_PORT = 0;
    /** IP address or host name */
    private String host;
    /** port number */
    private int port;
    private static final int FORMAT_HOST_PORT_FACTORY = 1;

}

