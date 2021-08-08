class LogStream {
    /**
     * Adjusts the current indentation level of this log stream.
     *
     * @param delta
     */
    public void adjustIndentation(int delta) {
	if (delta &lt; 0) {
	    indentationLevel = Math.max(0, indentationLevel + delta);
	} else {
	    indentationLevel += delta;
	}
    }

    private int indentationLevel;

}

