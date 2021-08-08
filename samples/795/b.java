import nsk.share.*;

class ByteBuffer {
    /**
     * Replace len bytes starting at offset off with the bytes from the
     * given byte array.
     *
     * @throws BoundException if offset and length are out of buffer bounds
     */
    public void putBytes(int off, byte[] value, int start, int len) throws BoundException {
	if (len &gt; (CurrentSize - off)) {
	    throw new BoundException("Unable to put " + len + " bytes at " + offsetString(off) + " (available bytes: "
		    + (CurrentSize - off) + ")");
	}
	try {
	    for (int i = 0; i &lt; len; i++)
		putByte(off++, value[start++]);
	} catch (BoundException e) {
	    throw new Failure("Caught unexpected bound exception while putting " + len + "bytes at " + offsetString(off)
		    + ":\n\t" + e);
	}
    }

    /**
     * Current number of bytes in the buffer.
     */
    private int CurrentSize;
    /**
     * Array of bytes in the buffer.
     */
    protected byte[] bytes;

    /**
     * Return string with hexadecimal representation of offset.
     */
    public static String offsetString(int off) {
	return "0x" + toHexString(off, 4);
    }

    /**
     * Replace the byte at the specified offset in this buffer with the
     * less significant byte from the int value.
     *
     * @throws BoundException if specified offset is out of buffer bounds
     */
    public void putByte(int off, byte value) throws BoundException {

	if ((off &lt; 0) || (off &gt;= CurrentSize))
	    throw new BoundException("Unable to put one byte at " + offsetString(off));

	bytes[off] = value;
    }

    /**
     * Return string with hexadecimal representation of bytes.
     */
    public static String toHexString(long b, int length) {
	return Right(Long.toHexString(b), length).replace(' ', '0');
    }

    private static String Right(String source, int length) {

	if (length &lt;= 0)
	    return "";

	if (length &lt;= source.length())
	    return source.substring(source.length() - length, source.length());
	else
	    return PadL(source, length);
    }

    private static String PadL(String source, int length) {
	return PadL(source, length, " ");
    }

    /**
     * Read count bytes (1-8) from this buffer at the current parser
     * position and returns a long value composed of these bytes.
     *
     * @throws BoundException if there are no so many bytes in the buffer
     */
    /*
    protected long getValueBytes(int count) throws BoundException {
        long value = getValueBytes(parseOffset);
        parseOffset += count;
        return value;
    }
     */

    // ---

    private static String PadL(String source, int length, String what) {

	if (length &lt;= 0)
	    return "";

	if (source.length() &gt; length)
	    return PadL("", length, "*");

	while (source.length() &lt; length)
	    source = what + source;

	return source;
    }

}

