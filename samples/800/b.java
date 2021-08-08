import nsk.share.*;

class SocketIOPipe extends Logger implements Finalizable {
    /**
     * Set ping timeout in milliseconds (0 means don't use ping at all).
     */
    public void setPingTimeout(long timeout) {
	if (connection == null) {
	    throw new TestBug("Attempt to set ping timeout for not established connection");
	}
	connection.setPingTimeout(timeout);
    }

    protected SocketConnection connection;

}

