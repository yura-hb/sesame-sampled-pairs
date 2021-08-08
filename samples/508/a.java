class HtmlTools {
    /**
     * Changes all unicode characters into &#xxx values.
     * Opposite to {@link HtmlTools#unescapeHTMLUnicodeEntity(String)}
     */
    public static String unicodeToHTMLUnicodeEntity(String text, boolean pPreserveNewlines) {
	/*
	 * Heuristic reserve for expansion : factor 1.2
	 */
	StringBuffer result = new StringBuffer((int) (text.length() * 1.2));
	int intValue;
	char myChar;
	for (int i = 0; i &lt; text.length(); ++i) {
	    myChar = text.charAt(i);
	    intValue = (int) text.charAt(i);
	    boolean outOfRange = intValue &lt; 32 || intValue &gt; 126;
	    if (pPreserveNewlines && myChar == '\n') {
		outOfRange = false;
	    }
	    if (pPreserveNewlines && myChar == '\r') {
		outOfRange = false;
	    }
	    if (outOfRange) {
		result.append("&#x").append(Integer.toString(intValue, 16)).append(';');
	    } else {
		result.append(myChar);
	    }
	}
	return result.toString();
    }

}

