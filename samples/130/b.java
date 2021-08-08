class BufferedReader extends Reader {
    /**
     * Reads a line of text.  A line is considered to be terminated by any one
     * of a line feed ('\n'), a carriage return ('\r'), a carriage return
     * followed immediately by a line feed, or by reaching the end-of-file
     * (EOF).
     *
     * @param      ignoreLF  If true, the next '\n' will be skipped
     *
     * @return     A String containing the contents of the line, not including
     *             any line-termination characters, or null if the end of the
     *             stream has been reached without reading any characters
     *
     * @see        java.io.LineNumberReader#readLine()
     *
     * @exception  IOException  If an I/O error occurs
     */
    String readLine(boolean ignoreLF) throws IOException {
	StringBuffer s = null;
	int startChar;

	synchronized (lock) {
	    ensureOpen();
	    boolean omitLF = ignoreLF || skipLF;

	    bufferLoop: for (;;) {

		if (nextChar &gt;= nChars)
		    fill();
		if (nextChar &gt;= nChars) { /* EOF */
		    if (s != null && s.length() &gt; 0)
			return s.toString();
		    else
			return null;
		}
		boolean eol = false;
		char c = 0;
		int i;

		/* Skip a leftover '\n', if necessary */
		if (omitLF && (cb[nextChar] == '\n'))
		    nextChar++;
		skipLF = false;
		omitLF = false;

		charLoop: for (i = nextChar; i &lt; nChars; i++) {
		    c = cb[i];
		    if ((c == '\n') || (c == '\r')) {
			eol = true;
			break charLoop;
		    }
		}

		startChar = nextChar;
		nextChar = i;

		if (eol) {
		    String str;
		    if (s == null) {
			str = new String(cb, startChar, i - startChar);
		    } else {
			s.append(cb, startChar, i - startChar);
			str = s.toString();
		    }
		    nextChar++;
		    if (c == '\r') {
			skipLF = true;
		    }
		    return str;
		}

		if (s == null)
		    s = new StringBuffer(defaultExpectedLineLength);
		s.append(cb, startChar, i - startChar);
	    }
	}
    }

    /** If the next character is a line feed, skip it */
    private boolean skipLF = false;
    private int nChars, nextChar;
    private int nChars, nextChar;
    private char cb[];
    private static int defaultExpectedLineLength = 80;
    private Reader in;
    private int markedChar = UNMARKED;
    private static final int UNMARKED = -1;
    private int readAheadLimit = 0;
    private static final int INVALIDATED = -2;

    /** Checks to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
	if (in == null)
	    throw new IOException("Stream closed");
    }

    /**
     * Fills the input buffer, taking the mark into account if it is valid.
     */
    private void fill() throws IOException {
	int dst;
	if (markedChar &lt;= UNMARKED) {
	    /* No mark */
	    dst = 0;
	} else {
	    /* Marked */
	    int delta = nextChar - markedChar;
	    if (delta &gt;= readAheadLimit) {
		/* Gone past read-ahead limit: Invalidate mark */
		markedChar = INVALIDATED;
		readAheadLimit = 0;
		dst = 0;
	    } else {
		if (readAheadLimit &lt;= cb.length) {
		    /* Shuffle in the current buffer */
		    System.arraycopy(cb, markedChar, cb, 0, delta);
		    markedChar = 0;
		    dst = delta;
		} else {
		    /* Reallocate buffer to accommodate read-ahead limit */
		    char ncb[] = new char[readAheadLimit];
		    System.arraycopy(cb, markedChar, ncb, 0, delta);
		    cb = ncb;
		    markedChar = 0;
		    dst = delta;
		}
		nextChar = nChars = delta;
	    }
	}

	int n;
	do {
	    n = in.read(cb, dst, cb.length - dst);
	} while (n == 0);
	if (n &gt; 0) {
	    nChars = dst + n;
	    nextChar = dst;
	}
    }

}

