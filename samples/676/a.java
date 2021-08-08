import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

class StringUtils {
    /**
     * Returns a collection of strings.
     * @param str comma separated string values
     * @return an &lt;code&gt;ArrayList&lt;/code&gt; of string values
     */
    public static Collection&lt;String&gt; getStringCollection(String str) {
	String delim = ",";
	return getStringCollection(str, delim);
    }

    /**
     * Returns a collection of strings.
     *
     * @param str
     *          String to parse
     * @param delim
     *          delimiter to separate the values
     * @return Collection of parsed elements.
     */
    public static Collection&lt;String&gt; getStringCollection(String str, String delim) {
	List&lt;String&gt; values = new ArrayList&lt;String&gt;();
	if (str == null)
	    return values;
	StringTokenizer tokenizer = new StringTokenizer(str, delim);
	while (tokenizer.hasMoreTokens()) {
	    values.add(tokenizer.nextToken());
	}
	return values;
    }

}

