class StrBuilder implements CharSequence, Appendable, Serializable, Builder&lt;String&gt; {
    /**
     * Gets the character at the specified index.
     *
     * @see #setCharAt(int, char)
     * @see #deleteCharAt(int)
     * @param index  the index to retrieve, must be valid
     * @return the character at the index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public char charAt(final int index) {
	if (index &lt; 0 || index &gt;= length()) {
	    throw new StringIndexOutOfBoundsException(index);
	}
	return buffer[index];
    }

    /** Internal data storage. */
    protected char[] buffer;
    /** Current size of the buffer. */
    protected int size;

    /**
     * Gets the length of the string builder.
     *
     * @return the length
     */
    @Override
    public int length() {
	return size;
    }

}

