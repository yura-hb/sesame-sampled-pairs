class Asserts {
    /**
     * Asserts that two strings are equal.
     *
     * If strings are not equals, then exception message
     * will contain {@code msg} followed by list of mismatched lines.
     *
     * @param str1 First string to compare.
     * @param str2 Second string to compare.
     * @param msg A description of the assumption.
     * @throws RuntimeException if strings are not equal.
     */
    public static void assertStringsEqual(String str1, String str2, String msg) {
	String lineSeparator = System.getProperty("line.separator");
	String str1Lines[] = str1.split(lineSeparator);
	String str2Lines[] = str2.split(lineSeparator);

	int minLength = Math.min(str1Lines.length, str2Lines.length);
	String longestStringLines[] = ((str1Lines.length == minLength) ? str2Lines : str1Lines);

	boolean stringsAreDifferent = false;

	StringBuilder messageBuilder = new StringBuilder(msg);

	messageBuilder.append("\n");

	for (int line = 0; line &lt; minLength; line++) {
	    if (!str1Lines[line].equals(str2Lines[line])) {
		messageBuilder.append(String.format("[line %d] '%s' differs " + "from '%s'\n", line, str1Lines[line],
			str2Lines[line]));
		stringsAreDifferent = true;
	    }
	}

	if (minLength &lt; longestStringLines.length) {
	    String stringName = ((longestStringLines == str1Lines) ? "first" : "second");
	    messageBuilder.append(String.format("Only %s string contains " + "following lines:\n", stringName));
	    stringsAreDifferent = true;
	    for (int line = minLength; line &lt; longestStringLines.length; line++) {
		messageBuilder.append(String.format("[line %d] '%s'", line, longestStringLines[line]));
	    }
	}

	if (stringsAreDifferent) {
	    fail(messageBuilder.toString());
	}
    }

    /**
     * Fail reports a failure with a message.
     * @param message for the failure
     * @throws RuntimeException always
     */
    public static void fail(String message) {
	throw new RuntimeException(message);
    }

}

