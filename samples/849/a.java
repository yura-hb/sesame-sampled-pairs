import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HtmlTools {
    /**
     * Removes all tags (&lt;..&gt;) from a string if it starts with "&lt;html&gt;..." to
     * make it compareable.
     */
    public static String removeHtmlTagsFromString(String text) {
	if (HtmlTools.isHtmlNode(text)) {
	    return removeAllTagsFromString(text); // (?s) enables that . matches
						  // newline.
	} else {
	    return text;
	}
    }

    private static final Pattern HTML_PATTERN = Pattern.compile("(?s).*&lt;\\s*html.*?&gt;.*");
    private static final Pattern TAGS_PATTERN = Pattern.compile("(?s)&lt;[^&gt;&lt;]*&gt;");

    /**
     * Searches for &lt;html&gt; tag in text.
     */
    public static boolean isHtmlNode(String text) {
	return HTML_PATTERN.matcher(text.toLowerCase(Locale.ENGLISH)).matches();
    }

    public static String removeAllTagsFromString(String text) {
	return TAGS_PATTERN.matcher(text).replaceAll("");
    }

}

