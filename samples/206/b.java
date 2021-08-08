class VirtualMachineImpl extends HotSpotVirtualMachine {
    /**
     * Detach from the target VM
     */
    public void detach() throws IOException {
	synchronized (this) {
	    if (socket_path != null) {
		socket_path = null;
	    }
	}
    }

    String socket_path;

}

