class TargetInterface {
    /**
    * Returns whether this interface is connected to the target.
    */
    boolean isConnected() {
	return this.socket != null;
    }

    /**
     * The connection to the target's ide interface.
     */
    Socket socket;

}

