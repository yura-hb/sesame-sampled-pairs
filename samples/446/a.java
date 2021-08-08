class CommonUtil {
    /**
     * Checks whether the given string is a valid identifier.
     * @param str A string to check.
     * @return true when the given string contains valid identifier.
     */
    public static boolean isIdentifier(String str) {
	boolean isIdentifier = !str.isEmpty();

	for (int i = 0; isIdentifier && i &lt; str.length(); i++) {
	    if (i == 0) {
		isIdentifier = Character.isJavaIdentifierStart(str.charAt(0));
	    } else {
		isIdentifier = Character.isJavaIdentifierPart(str.charAt(i));
	    }
	}

	return isIdentifier;
    }

}

