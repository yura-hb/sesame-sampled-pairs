import java.util.NoSuchElementException;

class ObjectArrayIterator&lt;E&gt; implements ResettableIterator&lt;E&gt; {
    /**
     * Returns the next element in the array.
     *
     * @return the next element in the array
     * @throws NoSuchElementException if all the elements in the array
     *    have already been returned
     */
    @Override
    public E next() {
	if (hasNext() == false) {
	    throw new NoSuchElementException();
	}
	return this.array[this.index++];
    }

    /** The array */
    final E[] array;
    /** The current iterator index */
    int index = 0;
    /** The end index to loop to */
    final int endIndex;

    /**
     * Returns true if there are more elements to return from the array.
     *
     * @return true if there is a next element to return
     */
    @Override
    public boolean hasNext() {
	return this.index &lt; this.endIndex;
    }

}

