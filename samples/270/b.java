class StrBuilder implements CharSequence, Appendable, Serializable, Builder&lt;String&gt; {
    /**
     * Gets a StringBuffer version of the string builder, creating a
     * new instance each time the method is called.
     *
     * @return the builder as a StringBuffer
     */
    public StringBuffer toStringBuffer() {
	return new StringBuffer(size).append(buffer, 0, size);
    }

    /** Current size of the buffer. */
    protected int size;
    /** Internal data storage. */
    protected char[] buffer;

}

