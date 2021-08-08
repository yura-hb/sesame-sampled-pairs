class StringUtils {
    /**
     * &lt;p&gt;Converts a String to lower case as per {@link String#toLowerCase(Locale)}.&lt;/p&gt;
     *
     * &lt;p&gt;A {@code null} input String returns {@code null}.&lt;/p&gt;
     *
     * &lt;pre&gt;
     * StringUtils.lowerCase(null, Locale.ENGLISH)  = null
     * StringUtils.lowerCase("", Locale.ENGLISH)    = ""
     * StringUtils.lowerCase("aBc", Locale.ENGLISH) = "abc"
     * &lt;/pre&gt;
     *
     * @param str  the String to lower case, may be null
     * @param locale  the locale that defines the case transformation rules, must not be null
     * @return the lower cased String, {@code null} if null String input
     * @since 2.5
     */
    public static String lowerCase(final String str, final Locale locale) {
	if (str == null) {
	    return null;
	}
	return str.toLowerCase(locale);
    }

}

