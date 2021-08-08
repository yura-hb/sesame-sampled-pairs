import java.util.ListIterator;

class ReverseListIterator&lt;E&gt; implements ResettableListIterator&lt;E&gt; {
    /**
     * Adds a new element to the list between the next and previous elements.
     *
     * @param obj  the object to add
     * @throws UnsupportedOperationException if the list is unmodifiable
     * @throws IllegalStateException if the iterator is not in a valid state for set
     */
    @Override
    public void add(final E obj) {
	// the validForUpdate flag is needed as the necessary previous()
	// method call re-enables remove and add
	if (validForUpdate == false) {
	    throw new IllegalStateException("Cannot add to list until next() or previous() called");
	}
	validForUpdate = false;
	iterator.add(obj);
	iterator.previous();
    }

    /** Flag to indicate if updating is possible at the moment. */
    private boolean validForUpdate = true;
    /** The list iterator being wrapped. */
    private ListIterator&lt;E&gt; iterator;

}

