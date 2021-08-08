class CharSequenceUtils {
    /**
     * Returns the index within &lt;code&gt;cs&lt;/code&gt; of the first occurrence of the
     * specified character, starting the search at the specified index.
     * &lt;p&gt;
     * If a character with value &lt;code&gt;searchChar&lt;/code&gt; occurs in the
     * character sequence represented by the &lt;code&gt;cs&lt;/code&gt;
     * object at an index no smaller than &lt;code&gt;start&lt;/code&gt;, then
     * the index of the first such occurrence is returned. For values
     * of &lt;code&gt;searchChar&lt;/code&gt; in the range from 0 to 0xFFFF (inclusive),
     * this is the smallest value &lt;i&gt;k&lt;/i&gt; such that:
     * &lt;blockquote&gt;&lt;pre&gt;
     * (this.charAt(&lt;i&gt;k&lt;/i&gt;) == searchChar) &amp;&amp; (&lt;i&gt;k&lt;/i&gt; &gt;= start)
     * &lt;/pre&gt;&lt;/blockquote&gt;
     * is true. For other values of &lt;code&gt;searchChar&lt;/code&gt;, it is the
     * smallest value &lt;i&gt;k&lt;/i&gt; such that:
     * &lt;blockquote&gt;&lt;pre&gt;
     * (this.codePointAt(&lt;i&gt;k&lt;/i&gt;) == searchChar) &amp;&amp; (&lt;i&gt;k&lt;/i&gt; &gt;= start)
     * &lt;/pre&gt;&lt;/blockquote&gt;
     * is true. In either case, if no such character occurs inm &lt;code&gt;cs&lt;/code&gt;
     * at or after position &lt;code&gt;start&lt;/code&gt;, then
     * &lt;code&gt;-1&lt;/code&gt; is returned.
     *
     * &lt;p&gt;
     * There is no restriction on the value of &lt;code&gt;start&lt;/code&gt;. If it
     * is negative, it has the same effect as if it were zero: the entire
     * &lt;code&gt;CharSequence&lt;/code&gt; may be searched. If it is greater than
     * the length of &lt;code&gt;cs&lt;/code&gt;, it has the same effect as if it were
     * equal to the length of &lt;code&gt;cs&lt;/code&gt;: &lt;code&gt;-1&lt;/code&gt; is returned.
     *
     * &lt;p&gt;All indices are specified in &lt;code&gt;char&lt;/code&gt; values
     * (Unicode code units).
     *
     * @param cs  the {@code CharSequence} to be processed, not null
     * @param searchChar  the char to be searched for
     * @param start  the start index, negative starts at the string start
     * @return the index where the search char was found, -1 if not found
     * @since 3.6 updated to behave more like &lt;code&gt;String&lt;/code&gt;
     */
    static int indexOf(final CharSequence cs, final int searchChar, int start) {
	if (cs instanceof String) {
	    return ((String) cs).indexOf(searchChar, start);
	}
	final int sz = cs.length();
	if (start &lt; 0) {
	    start = 0;
	}
	if (searchChar &lt; Character.MIN_SUPPLEMENTARY_CODE_POINT) {
	    for (int i = start; i &lt; sz; i++) {
		if (cs.charAt(i) == searchChar) {
		    return i;
		}
	    }
	}
	//supplementary characters (LANG1300)
	if (searchChar &lt;= Character.MAX_CODE_POINT) {
	    final char[] chars = Character.toChars(searchChar);
	    for (int i = start; i &lt; sz - 1; i++) {
		final char high = cs.charAt(i);
		final char low = cs.charAt(i + 1);
		if (high == chars[0] && low == chars[1]) {
		    return i;
		}
	    }
	}
	return NOT_FOUND;
    }

    private static final int NOT_FOUND = -1;

}

