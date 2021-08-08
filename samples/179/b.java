class StringUtils {
    /**
     * &lt;p&gt;Checks if the String contains only unicode letters.&lt;/p&gt;
     *
     * &lt;p&gt;&lt;code&gt;null&lt;/code&gt; will return &lt;code&gt;false&lt;/code&gt;.
     * An empty String (length()=0) will return &lt;code&gt;true&lt;/code&gt;.&lt;/p&gt;
     *
     * &lt;pre&gt;
     * StringUtils.isAlpha(null)   = false
     * StringUtils.isAlpha("")     = true
     * StringUtils.isAlpha("  ")   = false
     * StringUtils.isAlpha("abc")  = true
     * StringUtils.isAlpha("ab2c") = false
     * StringUtils.isAlpha("ab-c") = false
     * &lt;/pre&gt;
     *
     * @param str  the String to check, may be null
     * @return &lt;code&gt;true&lt;/code&gt; if only contains letters, and is non-null
     */
    public static boolean isAlpha(String str) {
	if (str == null) {
	    return false;
	}
	int sz = str.length();
	for (int i = 0; i &lt; sz; i++) {
	    if (!Character.isLetter(str.charAt(i))) {
		return false;
	    }
	}
	return true;
    }

}

