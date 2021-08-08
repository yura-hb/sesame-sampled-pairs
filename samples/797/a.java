import java.net.ServerSocket;

class Util {
    /**
    * Returns the next available port number on the local host.
    */
    public static int getFreePort() {
	ServerSocket socket = null;
	try {
	    socket = new ServerSocket(0);
	    return socket.getLocalPort();
	} catch (IOException e) {
	    // ignore
	} finally {
	    if (socket != null) {
		try {
		    socket.close();
		} catch (IOException e) {
		    // ignore
		}
	    }
	}
	return -1;
    }

}

