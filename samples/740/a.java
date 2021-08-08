class Util {
    /**
     * Returns whether the local file system supports accessing and modifying
     * the given attribute.
     */
    protected static boolean isAttributeSupported(int attribute) {
	return (EFS.getLocalFileSystem().attributes() & attribute) != 0;
    }

}

