import java.io.*;

class WritableUtils {
    /**
     * Skip &lt;i&gt;len&lt;/i&gt; number of bytes in input stream&lt;i&gt;in&lt;/i&gt;
     * @param in input stream
     * @param len number of bytes to skip
     * @throws IOException when skipped less number of bytes
     */
    public static void skipFully(DataInput in, int len) throws IOException {
	int total = 0;
	int cur = 0;

	while ((total &lt; len) && ((cur = in.skipBytes(len - total)) &gt; 0)) {
	    total += cur;
	}

	if (total &lt; len) {
	    throw new IOException("Not able to skip " + len + " bytes, possibly " + "due to end of input.");
	}
    }

}

