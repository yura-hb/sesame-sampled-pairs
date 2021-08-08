class DictionaryEntryLineParser {
    /**
     * Unescape input for CSV
     *
     * @param text  text to be unescaped
     * @return unescaped value, not null
     */
    public static String unescape(String text) {
	StringBuilder builder = new StringBuilder();
	boolean foundQuote = false;

	for (int i = 0; i &lt; text.length(); i++) {
	    char c = text.charAt(i);

	    if (i == 0 && c == QUOTE || i == text.length() - 1 && c == QUOTE) {
		continue;
	    }

	    if (c == QUOTE) {
		if (foundQuote) {
		    builder.append(QUOTE);
		    foundQuote = false;
		} else {
		    foundQuote = true;
		}
	    } else {
		foundQuote = false;
		builder.append(c);
	    }
	}

	return builder.toString();
    }

    private static final char QUOTE = '"';

}

