import java.util.regex.Pattern;

class JavadocUtil {
    /**
     * Replace all control chars with escaped symbols.
     * @param text the String to process.
     * @return the processed String with all control chars escaped.
     */
    public static String escapeAllControlChars(String text) {
	final String textWithoutNewlines = NEWLINE.matcher(text).replaceAll("\\\\n");
	final String textWithoutReturns = RETURN.matcher(textWithoutNewlines).replaceAll("\\\\r");
	return TAB.matcher(textWithoutReturns).replaceAll("\\\\t");
    }

    /** Newline pattern. */
    private static final Pattern NEWLINE = Pattern.compile("\n");
    /** Return pattern. */
    private static final Pattern RETURN = Pattern.compile("\r");
    /** Tab pattern. */
    private static final Pattern TAB = Pattern.compile("\t");

}

