class ArrayListIterator&lt;E&gt; extends ArrayIterator&lt;E&gt; implements ResettableListIterator&lt;E&gt; {
    /**
     * Gets the next index to be retrieved.
     *
     * @return the index of the item to be retrieved next
     */
    @Override
    public int nextIndex() {
	return this.index - this.startIndex;
    }

}

