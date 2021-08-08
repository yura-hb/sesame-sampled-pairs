class StringUtils {
    /**
     * &lt;p&gt;Checks if the CharSequence contains only Unicode letters.&lt;/p&gt;
     *
     * &lt;p&gt;{@code null} will return {@code false}.
     * An empty CharSequence (length()=0) will return {@code false}.&lt;/p&gt;
     *
     * &lt;pre&gt;
     * StringUtils.isAlpha(null)   = false
     * StringUtils.isAlpha("")     = false
     * StringUtils.isAlpha("  ")   = false
     * StringUtils.isAlpha("abc")  = true
     * StringUtils.isAlpha("ab2c") = false
     * StringUtils.isAlpha("ab-c") = false
     * &lt;/pre&gt;
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if only contains letters, and is non-null
     * @since 3.0 Changed signature from isAlpha(String) to isAlpha(CharSequence)
     * @since 3.0 Changed "" to return false and not true
     */
    public static boolean isAlpha(final CharSequence cs) {
	if (isEmpty(cs)) {
	    return false;
	}
	final int sz = cs.length();
	for (int i = 0; i &lt; sz; i++) {
	    if (!Character.isLetter(cs.charAt(i))) {
		return false;
	    }
	}
	return true;
    }

    /**
     * &lt;p&gt;Checks if a CharSequence is empty ("") or null.&lt;/p&gt;
     *
     * &lt;pre&gt;
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * &lt;/pre&gt;
     *
     * &lt;p&gt;NOTE: This method changed in Lang version 2.0.
     * It no longer trims the CharSequence.
     * That functionality is available in isBlank().&lt;/p&gt;
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is empty or null
     * @since 3.0 Changed signature from isEmpty(String) to isEmpty(CharSequence)
     */
    public static boolean isEmpty(final CharSequence cs) {
	return cs == null || cs.length() == 0;
    }

}

