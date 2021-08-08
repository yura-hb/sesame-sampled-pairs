import java.net.*;

class TargetInterface {
    /**
    * (PRIVATE API)
    * Disconnects this interface from the target.
    */
    public void disconnect() {
	if (this.socket != null) {
	    try {
		this.socket.close();
	    } catch (IOException e) {
		// Already closed. Nothing more to do
	    }
	    this.socket = null;
	}
    }

    /**
     * The connection to the target's ide interface.
     */
    Socket socket;

}

