import java.util.NoSuchElementException;

class IdentityLinkedList&lt;E&gt; extends AbstractSequentialList&lt;E&gt; implements List&lt;E&gt;, Deque&lt;E&gt; {
    /**
     * Returns the first element in this list.
     *
     * @return the first element in this list
     * @throws NoSuchElementException if this list is empty
     */
    public E getFirst() {
	if (size == 0)
	    throw new NoSuchElementException();

	return header.next.element;
    }

    private transient int size = 0;
    private transient Entry&lt;E&gt; header = new Entry&lt;E&gt;(null, null, null);

}

