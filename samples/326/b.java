import java.io.OutputStream;

class WriterToUTF8Buffered extends Writer implements WriterChain {
    /**
    * Write a string.
    *
    * @param  s  String to be written
    *
    * @exception  IOException  If an I/O error occurs
    */
    public void write(final String s) throws IOException {

	// We multiply the length by three since this is the maximum length
	// of the characters that we can put into the buffer.  It is possible
	// for each Unicode character to expand to three bytes.
	final int length = s.length();
	int lengthx3 = 3 * length;

	if (lengthx3 &gt;= BYTES_MAX - count) {
	    // The requested length is greater than the unused part of the buffer
	    flushBuffer();

	    if (lengthx3 &gt; BYTES_MAX) {
		/*
		 * The requested length exceeds the size of the buffer,
		 * so break it up in chunks that don't exceed the buffer size.
		 */
		final int start = 0;
		int split = length / CHARS_MAX;
		final int chunks;
		if (length % CHARS_MAX &gt; 0)
		    chunks = split + 1;
		else
		    chunks = split;
		int end_chunk = 0;
		for (int chunk = 1; chunk &lt;= chunks; chunk++) {
		    int start_chunk = end_chunk;
		    end_chunk = start + (int) ((((long) length) * chunk) / chunks);
		    s.getChars(start_chunk, end_chunk, m_inputChars, 0);
		    int len_chunk = (end_chunk - start_chunk);

		    // Adjust the end of the chunk if it ends on a high char
		    // of a Unicode surrogate pair and low char of the pair
		    // is not going to be in the same chunk
		    final char c = m_inputChars[len_chunk - 1];
		    if (c &gt;= 0xD800 && c &lt;= 0xDBFF) {
			// Exclude char in this chunk,
			// to avoid spanning a Unicode character
			// that is in two Java chars as a high/low surrogate
			end_chunk--;
			len_chunk--;
			if (chunk == chunks) {
			    /* TODO: error message needed.
			     * The String incorrectly ends in a high char
			     * of a high/low surrogate pair, but there is
			     * no corresponding low as the high is the last char
			     * Recover by ignoring this last char.
			     */
			}
		    }

		    this.write(m_inputChars, 0, len_chunk);
		}
		return;
	    }
	}

	s.getChars(0, length, m_inputChars, 0);
	final char[] chars = m_inputChars;
	final int n = length;
	final byte[] buf_loc = m_outputBytes; // local reference for faster access
	int count_loc = count; // local integer for faster access
	int i = 0;
	{
	    /* This block could be omitted and the code would produce
	     * the same result. But this block exists to give the JIT
	     * a better chance of optimizing a tight and common loop which
	     * occurs when writing out ASCII characters.
	     */
	    char c;
	    for (; i &lt; n && (c = chars[i]) &lt; 0x80; i++)
		buf_loc[count_loc++] = (byte) c;
	}
	for (; i &lt; n; i++) {

	    final char c = chars[i];

	    if (c &lt; 0x80)
		buf_loc[count_loc++] = (byte) (c);
	    else if (c &lt; 0x800) {
		buf_loc[count_loc++] = (byte) (0xc0 + (c &gt;&gt; 6));
		buf_loc[count_loc++] = (byte) (0x80 + (c & 0x3f));
	    }
	    /**
	      * The following else if condition is added to support XML 1.1 Characters for
	      * UTF-8:   [1111 0uuu] [10uu zzzz] [10yy yyyy] [10xx xxxx]*
	      * Unicode: [1101 10ww] [wwzz zzyy] (high surrogate)
	      *          [1101 11yy] [yyxx xxxx] (low surrogate)
	      *          * uuuuu = wwww + 1
	      */
	    else if (c &gt;= 0xD800 && c &lt;= 0xDBFF) {
		char high, low;
		high = c;
		i++;
		low = chars[i];

		buf_loc[count_loc++] = (byte) (0xF0 | (((high + 0x40) &gt;&gt; 8) & 0xf0));
		buf_loc[count_loc++] = (byte) (0x80 | (((high + 0x40) &gt;&gt; 2) & 0x3f));
		buf_loc[count_loc++] = (byte) (0x80 | ((low &gt;&gt; 6) & 0x0f) + ((high &lt;&lt; 4) & 0x30));
		buf_loc[count_loc++] = (byte) (0x80 | (low & 0x3f));
	    } else {
		buf_loc[count_loc++] = (byte) (0xe0 + (c &gt;&gt; 12));
		buf_loc[count_loc++] = (byte) (0x80 + ((c &gt;&gt; 6) & 0x3f));
		buf_loc[count_loc++] = (byte) (0x80 + (c & 0x3f));
	    }
	}
	// Store the local integer back into the instance variable
	count = count_loc;

    }

    /** number of bytes that the byte buffer can hold.
    * This is a fixed constant is used rather than m_outputBytes.lenght for performance.
    */
    private static final int BYTES_MAX = 16 * 1024;
    /**
    * The number of valid bytes in the buffer. This value is always
    * in the range &lt;tt&gt;0&lt;/tt&gt; through &lt;tt&gt;m_outputBytes.length&lt;/tt&gt;; elements
    * &lt;tt&gt;m_outputBytes[0]&lt;/tt&gt; through &lt;tt&gt;m_outputBytes[count-1]&lt;/tt&gt; contain valid
    * byte data.
    */
    private int count;
    /** number of characters that the character buffer can hold.
    * This is 1/3 of the number of bytes because UTF-8 encoding
    * can expand one unicode character by up to 3 bytes.
    */
    private static final int CHARS_MAX = (BYTES_MAX / 3);
    private final char m_inputChars[];
    /**
    * The internal buffer where data is stored.
    * (sc & sb remove final to compile in JDK 1.1.8)
    */
    private final byte m_outputBytes[];
    /** The byte stream to write to. (sc & sb remove final to compile in JDK 1.1.8) */
    private final OutputStream m_os;

    /**
    * Flush the internal buffer
    *
    * @throws IOException
    */
    public void flushBuffer() throws IOException {

	if (count &gt; 0) {
	    m_os.write(m_outputBytes, 0, count);

	    count = 0;
	}
    }

    /**
    * Write a portion of an array of characters.
    *
    * @param  chars  Array of characters
    * @param  start   Offset from which to start writing characters
    * @param  length   Number of characters to write
    *
    * @exception  IOException  If an I/O error occurs
    *
    * @throws java.io.IOException
    */
    public void write(final char chars[], final int start, final int length) throws java.io.IOException {

	// We multiply the length by three since this is the maximum length
	// of the characters that we can put into the buffer.  It is possible
	// for each Unicode character to expand to three bytes.

	int lengthx3 = 3 * length;

	if (lengthx3 &gt;= BYTES_MAX - count) {
	    // The requested length is greater than the unused part of the buffer
	    flushBuffer();

	    if (lengthx3 &gt; BYTES_MAX) {
		/*
		 * The requested length exceeds the size of the buffer.
		 * Cut the buffer up into chunks, each of which will
		 * not cause an overflow to the output buffer m_outputBytes,
		 * and make multiple recursive calls.
		 * Be careful about integer overflows in multiplication.
		 */
		int split = length / CHARS_MAX;
		final int chunks;
		if (length % CHARS_MAX &gt; 0)
		    chunks = split + 1;
		else
		    chunks = split;
		int end_chunk = start;
		for (int chunk = 1; chunk &lt;= chunks; chunk++) {
		    int start_chunk = end_chunk;
		    end_chunk = start + (int) ((((long) length) * chunk) / chunks);

		    // Adjust the end of the chunk if it ends on a high char
		    // of a Unicode surrogate pair and low char of the pair
		    // is not going to be in the same chunk
		    final char c = chars[end_chunk - 1];
		    int ic = chars[end_chunk - 1];
		    if (c &gt;= 0xD800 && c &lt;= 0xDBFF) {
			// The last Java char that we were going
			// to process is the first of a
			// Java surrogate char pair that
			// represent a Unicode character.

			if (end_chunk &lt; start + length) {
			    // Avoid spanning by including the low
			    // char in the current chunk of chars.
			    end_chunk++;
			} else {
			    /* This is the last char of the last chunk,
			     * and it is the high char of a high/low pair with
			     * no low char provided.
			     * TODO: error message needed.
			     * The char array incorrectly ends in a high char
			     * of a high/low surrogate pair, but there is
			     * no corresponding low as the high is the last char
			     */
			    end_chunk--;
			}
		    }

		    int len_chunk = (end_chunk - start_chunk);
		    this.write(chars, start_chunk, len_chunk);
		}
		return;
	    }
	}

	final int n = length + start;
	final byte[] buf_loc = m_outputBytes; // local reference for faster access
	int count_loc = count; // local integer for faster access
	int i = start;
	{
	    /* This block could be omitted and the code would produce
	     * the same result. But this block exists to give the JIT
	     * a better chance of optimizing a tight and common loop which
	     * occurs when writing out ASCII characters.
	     */
	    char c;
	    for (; i &lt; n && (c = chars[i]) &lt; 0x80; i++)
		buf_loc[count_loc++] = (byte) c;
	}
	for (; i &lt; n; i++) {

	    final char c = chars[i];

	    if (c &lt; 0x80)
		buf_loc[count_loc++] = (byte) (c);
	    else if (c &lt; 0x800) {
		buf_loc[count_loc++] = (byte) (0xc0 + (c &gt;&gt; 6));
		buf_loc[count_loc++] = (byte) (0x80 + (c & 0x3f));
	    }
	    /**
	      * The following else if condition is added to support XML 1.1 Characters for
	      * UTF-8:   [1111 0uuu] [10uu zzzz] [10yy yyyy] [10xx xxxx]*
	      * Unicode: [1101 10ww] [wwzz zzyy] (high surrogate)
	      *          [1101 11yy] [yyxx xxxx] (low surrogate)
	      *          * uuuuu = wwww + 1
	      */
	    else if (c &gt;= 0xD800 && c &lt;= 0xDBFF) {
		char high, low;
		high = c;
		i++;
		low = chars[i];

		buf_loc[count_loc++] = (byte) (0xF0 | (((high + 0x40) &gt;&gt; 8) & 0xf0));
		buf_loc[count_loc++] = (byte) (0x80 | (((high + 0x40) &gt;&gt; 2) & 0x3f));
		buf_loc[count_loc++] = (byte) (0x80 | ((low &gt;&gt; 6) & 0x0f) + ((high &lt;&lt; 4) & 0x30));
		buf_loc[count_loc++] = (byte) (0x80 | (low & 0x3f));
	    } else {
		buf_loc[count_loc++] = (byte) (0xe0 + (c &gt;&gt; 12));
		buf_loc[count_loc++] = (byte) (0x80 + ((c &gt;&gt; 6) & 0x3f));
		buf_loc[count_loc++] = (byte) (0x80 + (c & 0x3f));
	    }
	}
	// Store the local integer back into the instance variable
	count = count_loc;

    }

}

