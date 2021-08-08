import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

class ListIteratorWrapper&lt;E&gt; implements ResettableListIterator&lt;E&gt; {
    /**
     * Returns the previous element.
     *
     * @return the previous element
     * @throws NoSuchElementException  if there are no previous elements
     */
    @Override
    public E previous() throws NoSuchElementException {
	if (iterator instanceof ListIterator) {
	    @SuppressWarnings("unchecked")
	    final ListIterator&lt;E&gt; li = (ListIterator&lt;E&gt;) iterator;
	    return li.previous();
	}

	if (currentIndex == 0) {
	    throw new NoSuchElementException();
	}
	removeState = wrappedIteratorIndex == currentIndex;
	return list.get(--currentIndex);
    }

    /** The underlying iterator being decorated. */
    private final Iterator&lt;? extends E&gt; iterator;
    /** The current index of this iterator. */
    private int currentIndex = 0;
    /** recall whether the wrapped iterator's "cursor" is in such a state as to allow remove() to be called */
    private boolean removeState;
    /** The current index of the wrapped iterator. */
    private int wrappedIteratorIndex = 0;
    /** The list being used to cache the iterator. */
    private final List&lt;E&gt; list = new ArrayList&lt;&gt;();

}

