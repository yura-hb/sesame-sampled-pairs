class StringEncoder {
    /**
     * Decode a string encoded by {@link #encode}.
     * 
     * &lt;p&gt;
     * The parsing is strict; any ill-formed backslash escape sequence (i.e.,
     * not of the form &lt;code&gt;&#92;uNNNN&lt;/code&gt; or &lt;code&gt;\\&lt;/code&gt;) will cause an
     * exception to be thrown.
     * 
     * @param text
     *            string to decode (possibly null)
     * @return the decoded version of {@code text}, or {@code null} if
     *         {@code text} was {@code null}
     * @throws IllegalArgumentException
     *             if {@code text} contains an invalid escape sequence
     * @see #encode
     */
    public static String decode(String text) {
	if (text == null)
	    return null;
	StringBuilder buf = new StringBuilder(text.length());
	final int limit = text.length();
	for (int i = 0; i &lt; limit; i++) {
	    char ch = text.charAt(i);

	    // Handle unescaped characters
	    if (ch != '\\') {
		buf.append(ch);
		continue;
	    }

	    // Get next char
	    if (++i &gt;= limit)
		throw new IllegalArgumentException("illegal trailing '\\' in encoded string");
	    ch = text.charAt(i);

	    // Check for backslash escape
	    if (ch == '\\') {
		buf.append(ch);
		continue;
	    }

	    // Must be unicode escape
	    if (ch != 'u')
		throw new IllegalArgumentException("illegal escape sequence '\\" + ch + "' in encoded string");

	    // Decode hex value
	    int value = 0;
	    for (int j = 0; j &lt; 4; j++) {
		if (++i &gt;= limit)
		    throw new IllegalArgumentException("illegal truncated '\\u' escape sequence in encoded string");
		int nibble = Character.digit(text.charAt(i), 16);
		if (nibble == -1) {
		    throw new IllegalArgumentException(
			    "illegal escape sequence '" + text.substring(i - j - 2, i - j + 4) + "' in encoded string");
		}
		// assert nibble &gt;= 0 && nibble &lt;= 0xf;
		value = (value &lt;&lt; 4) | nibble;
	    }

	    // Append decodec character
	    buf.append((char) value);
	}
	return buf.toString();
    }

}

