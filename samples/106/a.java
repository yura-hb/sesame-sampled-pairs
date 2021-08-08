class Buffer implements Comparable, Cloneable {
    /**
     * Get the data from the Buffer.
     *
     * @return The data is only valid between 0 and getCount() - 1.
     */
    public byte[] get() {
	if (bytes == null) {
	    bytes = new byte[0];
	}
	return bytes;
    }

    /** Backing store for Buffer. */
    private byte[] bytes = null;

}

