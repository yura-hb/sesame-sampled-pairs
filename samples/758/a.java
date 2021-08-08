class CharUtils {
    /**
     * &lt;p&gt;Converts the String to a char using the first character, throwing
     * an exception on empty Strings.&lt;/p&gt;
     *
     * &lt;pre&gt;
     *   CharUtils.toChar("A")  = 'A'
     *   CharUtils.toChar("BA") = 'B'
     *   CharUtils.toChar(null) throws IllegalArgumentException
     *   CharUtils.toChar("")   throws IllegalArgumentException
     * &lt;/pre&gt;
     *
     * @param str  the character to convert
     * @return the char value of the first letter of the String
     * @throws IllegalArgumentException if the String is empty
     */
    public static char toChar(final String str) {
	Validate.isTrue(StringUtils.isNotEmpty(str), "The String must not be empty");
	return str.charAt(0);
    }

}

