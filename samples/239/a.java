import java.util.NoSuchElementException;

class ObjectArrayListIterator&lt;E&gt; extends ObjectArrayIterator&lt;E&gt; implements ResettableListIterator&lt;E&gt; {
    /**
     * Gets the previous element from the array.
     *
     * @return the previous element
     * @throws NoSuchElementException if there is no previous element
     */
    @Override
    public E previous() {
	if (hasPrevious() == false) {
	    throw new NoSuchElementException();
	}
	this.lastItemIndex = --this.index;
	return this.array[this.index];
    }

    /**
     * Holds the index of the last item returned by a call to &lt;code&gt;next()&lt;/code&gt;
     * or &lt;code&gt;previous()&lt;/code&gt;. This is set to &lt;code&gt;-1&lt;/code&gt; if neither method
     * has yet been invoked. &lt;code&gt;lastItemIndex&lt;/code&gt; is used to to implement the
     * {@link #set} method.
     */
    private int lastItemIndex = -1;

    /**
     * Returns true if there are previous elements to return from the array.
     *
     * @return true if there is a previous element to return
     */
    @Override
    public boolean hasPrevious() {
	return this.index &gt; getStartIndex();
    }

}

