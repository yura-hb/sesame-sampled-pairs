class Frame&lt;V&gt; {
    /**
     * Returns the value of the given local variable.
     *
     * @param i
     *            a local variable index.
     * @return the value of the given local variable.
     * @throws IndexOutOfBoundsException
     *             if the variable does not exist.
     */
    public V getLocal(final int i) throws IndexOutOfBoundsException {
	if (i &gt;= locals) {
	    throw new IndexOutOfBoundsException("Trying to access an inexistant local variable");
	}
	return values[i];
    }

    /**
     * The number of local variables of this frame.
     */
    private int locals;
    /**
     * The local variables and operand stack of this frame.
     */
    private V[] values;

}

