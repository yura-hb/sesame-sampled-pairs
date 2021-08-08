import java.nio.ByteBuffer;

class Buffer {
    /**
     * Copies the data from this buffer into a given array.
     *
     * @param dst the destination array
     * @param off starting position in {@code dst}
     * @param len number of bytes to copy
     */
    public void copyInto(byte[] dst, int off, int len) {
	System.arraycopy(data.array(), 0, dst, off, len);
    }

    protected ByteBuffer data;

}

