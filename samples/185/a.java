import java.util.Map;

class CharEscaperBuilder {
    /** Add a new mapping from an index to an object to the escaping. */
    @CanIgnoreReturnValue
    public CharEscaperBuilder addEscape(char c, String r) {
	map.put(c, checkNotNull(r));
	if (c &gt; max) {
	    max = c;
	}
	return this;
    }

    private final Map&lt;Character, String&gt; map;
    private int max = -1;

}

