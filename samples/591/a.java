class CollatingIterator&lt;E&gt; implements Iterator&lt;E&gt; {
    /**
     * Returns the index of the iterator that returned the last element.
     *
     * @return the index of the iterator that returned the last element
     * @throws IllegalStateException if there is no last returned element
     */
    public int getIteratorIndex() {
	if (lastReturned == -1) {
	    throw new IllegalStateException("No value has been returned yet");
	}

	return lastReturned;
    }

    /**
     * Index of the {@link #iterators iterator} from whom the last returned
     * value was obtained.
     */
    private int lastReturned = -1;

}

