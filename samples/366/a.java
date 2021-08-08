class NumberUtils {
    /**
     * &lt;p&gt;Convert a &lt;code&gt;String&lt;/code&gt; to a &lt;code&gt;Integer&lt;/code&gt;, handling
     * hex (0xhhhh) and octal (0dddd) notations.
     * N.B. a leading zero means octal; spaces are not trimmed.&lt;/p&gt;
     *
     * &lt;p&gt;Returns &lt;code&gt;null&lt;/code&gt; if the string is &lt;code&gt;null&lt;/code&gt;.&lt;/p&gt;
     *
     * @param str  a &lt;code&gt;String&lt;/code&gt; to convert, may be null
     * @return converted &lt;code&gt;Integer&lt;/code&gt; (or null if the input is null)
     * @throws NumberFormatException if the value cannot be converted
     */
    public static Integer createInteger(final String str) {
	if (str == null) {
	    return null;
	}
	// decode() handles 0xAABD and 0777 (hex and octal) as well.
	return Integer.decode(str);
    }

}

