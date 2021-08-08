class BufferManager {
    /**
    * Returns the default buffer manager.
    */
    public synchronized static BufferManager getDefaultBufferManager() {
	if (DEFAULT_BUFFER_MANAGER == null) {
	    DEFAULT_BUFFER_MANAGER = new BufferManager();
	}
	return DEFAULT_BUFFER_MANAGER;
    }

    protected static BufferManager DEFAULT_BUFFER_MANAGER;

}

