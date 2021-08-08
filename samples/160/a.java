class StringUtils {
    /**
     * @param charsToEscape array of characters to be escaped
     */
    public static String escapeString(String str, char escapeChar, char[] charsToEscape) {
	if (str == null) {
	    return null;
	}
	StringBuilder result = new StringBuilder();
	for (int i = 0; i &lt; str.length(); i++) {
	    char curChar = str.charAt(i);
	    if (curChar == escapeChar || hasChar(charsToEscape, curChar)) {
		// special char
		result.append(escapeChar);
	    }
	    result.append(curChar);
	}
	return result.toString();
    }

    private static boolean hasChar(char[] chars, char character) {
	for (char target : chars) {
	    if (character == target) {
		return true;
	    }
	}
	return false;
    }

}

