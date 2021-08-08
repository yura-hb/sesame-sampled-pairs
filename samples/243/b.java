abstract class Transport extends Logger {
    /**
     * Send the specified bytes.
     */
    public void write(byte[] b, int off, int len) throws IOException {
	for (int i = 0; i &lt; len; i++)
	    write(b[off + i]);
    }

    /**
     * Send the specified byte.
     */
    public abstract void write(int b) throws IOException;

}

