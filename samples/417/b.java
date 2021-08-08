class UnixNativeDispatcher {
    /**
     * close(int filedes). If fd is -1 this is a no-op.
     */
    static void close(int fd) {
	if (fd != -1) {
	    close0(fd);
	}
    }

    private static native void close0(int fd);

}

