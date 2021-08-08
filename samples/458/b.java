import java.util.ArrayList;
import java.util.List;

class DictionaryEntryLineParser {
    /**
     * Parse CSV line
     *
     * @param line  line to parse
     * @return String array of parsed valued, null
     * @throws RuntimeException on malformed input
     */
    public static String[] parseLine(String line) {
	boolean insideQuote = false;
	List&lt;String&gt; result = new ArrayList&lt;&gt;();
	StringBuilder builder = new StringBuilder();
	int quoteCount = 0;

	for (int i = 0; i &lt; line.length(); i++) {
	    char c = line.charAt(i);

	    if (c == QUOTE) {
		insideQuote = !insideQuote;
		quoteCount++;
	    }

	    if (c == COMMA && !insideQuote) {
		String value = builder.toString();
		value = unescape(value);

		result.add(value);
		builder = new StringBuilder();
		continue;
	    }

	    builder.append(c);
	}

	result.add(builder.toString());

	if (quoteCount % 2 != 0) {
	    throw new RuntimeException("Unmatched quote in entry: " + line);
	}

	return result.toArray(new String[result.size()]);
    }

    private static final char QUOTE = '"';
    private static final char COMMA = ',';

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

}

