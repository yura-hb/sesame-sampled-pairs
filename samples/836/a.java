import java.io.InputStream;

class ByteStreams {
    /**
    * Reads some bytes from an input stream and stores them into the buffer array {@code b}. This
    * method blocks until {@code len} bytes of input data have been read into the array, or end of
    * file is detected. The number of bytes read is returned, possibly zero. Does not close the
    * stream.
    *
    * &lt;p&gt;A caller can detect EOF if the number of bytes read is less than {@code len}. All subsequent
    * calls on the same stream will return zero.
    *
    * &lt;p&gt;If {@code b} is null, a {@code NullPointerException} is thrown. If {@code off} is negative,
    * or {@code len} is negative, or {@code off+len} is greater than the length of the array {@code
    * b}, then an {@code IndexOutOfBoundsException} is thrown. If {@code len} is zero, then no bytes
    * are read. Otherwise, the first byte read is stored into element {@code b[off]}, the next one
    * into {@code b[off+1]}, and so on. The number of bytes read is, at most, equal to {@code len}.
    *
    * @param in the input stream to read from
    * @param b the buffer into which the data is read
    * @param off an int specifying the offset into the data
    * @param len an int specifying the number of bytes to read
    * @return the number of bytes read
    * @throws IOException if an I/O error occurs
    */
    @CanIgnoreReturnValue
    // Sometimes you don't care how many bytes you actually read, I guess.
    // (You know that it's either going to read len bytes or stop at EOF.)
    public static int read(InputStream in, byte[] b, int off, int len) throws IOException {
	checkNotNull(in);
	checkNotNull(b);
	if (len &lt; 0) {
	    throw new IndexOutOfBoundsException("len is negative");
	}
	int total = 0;
	while (total &lt; len) {
	    int result = in.read(b, off + total, len - total);
	    if (result == -1) {
		break;
	    }
	    total += result;
	}
	return total;
    }

}

