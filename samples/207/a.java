abstract class ToStringStyle implements Serializable {
    /**
     * &lt;p&gt;Remove the last field separator from the buffer.&lt;/p&gt;
     *
     * @param buffer  the &lt;code&gt;StringBuffer&lt;/code&gt; to populate
     * @since 2.0
     */
    protected void removeLastFieldSeparator(final StringBuffer buffer) {
	final int len = buffer.length();
	final int sepLen = fieldSeparator.length();
	if (len &gt; 0 && sepLen &gt; 0 && len &gt;= sepLen) {
	    boolean match = true;
	    for (int i = 0; i &lt; sepLen; i++) {
		if (buffer.charAt(len - 1 - i) != fieldSeparator.charAt(sepLen - 1 - i)) {
		    match = false;
		    break;
		}
	    }
	    if (match) {
		buffer.setLength(len - sepLen);
	    }
	}
    }

    /**
     * The field separator &lt;code&gt;','&lt;/code&gt;.
     */
    private String fieldSeparator = ",";

}

