class SingletonListIterator&lt;E&gt; implements ResettableListIterator&lt;E&gt; {
    /**
     * Returns the index of the element that would be returned by a subsequent
     * call to {@code next}.
     *
     * @return 0 or 1 depending on current state.
     */
    @Override
    public int nextIndex() {
	return beforeFirst ? 0 : 1;
    }

    private boolean beforeFirst = true;

}

