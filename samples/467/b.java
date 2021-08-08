abstract class FileSystem {
    /**
     * Opens the file system
     */
    public static FileSystem open() {
	synchronized (lock) {
	    if (fs == null) {
		fs = new FileSystemImpl();
	    }
	    return fs;
	}
    }

    private static final Object lock = new Object();
    private static FileSystem fs;

}

