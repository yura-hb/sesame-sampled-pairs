class CSS implements Serializable {
    /**
     * Sets the base font size from the passed in string.
     */
    void setBaseFontSize(String size) {
	int relSize, absSize, diff;

	if (size != null) {
	    if (size.startsWith("+")) {
		relSize = Integer.valueOf(size.substring(1)).intValue();
		setBaseFontSize(baseFontSize + relSize);
	    } else if (size.startsWith("-")) {
		relSize = -Integer.valueOf(size.substring(1)).intValue();
		setBaseFontSize(baseFontSize + relSize);
	    } else {
		setBaseFontSize(Integer.valueOf(size).intValue());
	    }
	}
    }

    /** Size used for relative units. */
    private int baseFontSize;

    /**
     * Sets the base font size. &lt;code&gt;sz&lt;/code&gt; is a CSS value, and is
     * not necessarily the point size. Use getPointSize to determine the
     * point size corresponding to &lt;code&gt;sz&lt;/code&gt;.
     */
    void setBaseFontSize(int sz) {
	if (sz &lt; 1)
	    baseFontSize = 0;
	else if (sz &gt; 7)
	    baseFontSize = 7;
	else
	    baseFontSize = sz;
    }

}

