class StrBuilder implements CharSequence, Appendable, Serializable, Builder&lt;String&gt; {
    /**
     * Searches the string builder to find the last reference to the specified char.
     *
     * @param ch  the character to find
     * @return the last index of the character, or -1 if not found
     */
    public int lastIndexOf(final char ch) {
	return lastIndexOf(ch, size - 1);
    }

    /** Current size of the buffer. */
    protected int size;
    /** Internal data storage. */
    protected char[] buffer;

    /**
     * Searches the string builder to find the last reference to the specified char.
     *
     * @param ch  the character to find
     * @param startIndex  the index to start at, invalid index rounded to edge
     * @return the last index of the character, or -1 if not found
     */
    public int lastIndexOf(final char ch, int startIndex) {
	startIndex = (startIndex &gt;= size ? size - 1 : startIndex);
	if (startIndex &lt; 0) {
	    return -1;
	}
	for (int i = startIndex; i &gt;= 0; i--) {
	    if (buffer[i] == ch) {
		return i;
	    }
	}
	return -1;
    }

}

