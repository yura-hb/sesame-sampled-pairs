class IndentPrinter extends Printer {
    /**
     * Decrement the indentation for the next line.
     */
    public void unindent() {
	_nextIndent -= _format.getIndent();
	if (_nextIndent &lt; 0)
	    _nextIndent = 0;
	// If there is no current line and we're de-identing then
	// this indentation level is actually the next level.
	if ((_line.length() + _spaces + _text.length()) == 0)
	    _thisIndent = _nextIndent;
    }

    /**
     * Holds the indentation for the next line to be printed. After this line is
     * printed, {@link #_nextIndent} is assigned to {@link #_thisIndent}.
     */
    private int _nextIndent;
    /**
     * Holds the currently accumulating text line. This buffer will constantly
     * be reused by deleting its contents instead of reallocating it.
     */
    private StringBuffer _line;
    /**
     * Counts how many white spaces come between the accumulated line and the
     * current accumulated text. Multiple spaces at the end of the a line
     * will not be printed.
     */
    private int _spaces;
    /**
     * Holds the currently accumulating text that follows {@link #_line}.
     * When the end of the part is identified by a call to {@link #printSpace}
     * or {@link #breakLine}, this part is added to the accumulated line.
     */
    private StringBuffer _text;
    /**
     * Holds the indentation for the current line that is now accumulating in
     * memory and will be sent for printing shortly.
     */
    private int _thisIndent;

}

