class Buffer implements Comparable, Cloneable {
    /**
     * Use the specified bytes array as underlying sequence.
     *
     * @param bytes byte sequence
     */
    public void set(byte[] bytes) {
	this.count = (bytes == null) ? 0 : bytes.length;
	this.bytes = bytes;
    }

    /** Number of valid bytes in this.bytes. */
    private int count;
    /** Backing store for Buffer. */
    private byte[] bytes = null;

}

