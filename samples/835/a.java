class PushbackReader extends FilterReader {
    /**
     * Tells whether this stream is ready to be read.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public boolean ready() throws IOException {
	synchronized (lock) {
	    ensureOpen();
	    return (pos &lt; buf.length) || super.ready();
	}
    }

    /** Current position in buffer */
    private int pos;
    /** Pushback buffer */
    private char[] buf;

    /** Checks to make sure that the stream has not been closed. */
    private void ensureOpen() throws IOException {
	if (buf == null)
	    throw new IOException("Stream closed");
    }

}

