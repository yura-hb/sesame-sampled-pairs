import java.io.FilterInputStream;
import java.io.InputStream;

class HashingInputStream extends FilterInputStream {
    /**
    * Reads the next byte of data from the underlying input stream and updates the hasher with the
    * byte read.
    */
    @Override
    @CanIgnoreReturnValue
    public int read() throws IOException {
	int b = in.read();
	if (b != -1) {
	    hasher.putByte((byte) b);
	}
	return b;
    }

    private final Hasher hasher;

}

