class CharUtils {
    /**
     * &lt;p&gt;Converts the string to the Unicode format '\u0020'.&lt;/p&gt;
     *
     * &lt;p&gt;This format is the Java source code format.&lt;/p&gt;
     *
     * &lt;pre&gt;
     *   CharUtils.unicodeEscaped(' ') = "\u0020"
     *   CharUtils.unicodeEscaped('A') = "\u0041"
     * &lt;/pre&gt;
     *
     * @param ch  the character to convert
     * @return the escaped Unicode string
     */
    public static String unicodeEscaped(final char ch) {
	return "\\u" + HEX_DIGITS[(ch &gt;&gt; 12) & 15] + HEX_DIGITS[(ch &gt;&gt; 8) & 15] + HEX_DIGITS[(ch &gt;&gt; 4) & 15]
		+ HEX_DIGITS[(ch) & 15];
    }

    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
	    'e', 'f' };

}

