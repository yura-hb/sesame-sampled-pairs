class FilterIterator&lt;E&gt; implements Iterator&lt;E&gt; {
    /**
     * Sets the iterator for this iterator to use.
     * If iteration has started, this effectively resets the iterator.
     *
     * @param iterator  the iterator to use
     */
    public void setIterator(final Iterator&lt;? extends E&gt; iterator) {
	this.iterator = iterator;
	nextObject = null;
	nextObjectSet = false;
    }

    /** The iterator being used */
    private Iterator&lt;? extends E&gt; iterator;
    /** The next object in the iteration */
    private E nextObject;
    /** Whether the next object has been calculated yet */
    private boolean nextObjectSet = false;

}

