import gnu.trove.list.TLinkable;

class TLinkedList&lt;T&gt; extends AbstractSequentialList&lt;T&gt; implements Externalizable {
    class IteratorImpl implements ListIterator&lt;T&gt; {
	/**
	 * Insert &lt;tt&gt;linkable&lt;/tt&gt; at the current position of the iterator.
	 * Calling next() after add() will return the added object.
	 *
	 * @param linkable an object of type TLinkable
	 */
	public final void add(T linkable) {
	    _lastReturned = null;
	    _nextIndex++;

	    if (_size == 0) {
		TLinkedList.this.add(linkable);
	    } else {
		TLinkedList.this.addBefore(_next, linkable);
	    }
	}

	private T _lastReturned;
	private int _nextIndex = 0;
	private T _next;

    }

    /** the number of elements in the list */
    protected int _size = 0;
    /** the head of the list */
    protected T _head;
    /** the tail of the list */
    protected T _tail;

    /**
     * Appends &lt;tt&gt;linkable&lt;/tt&gt; to the end of the list.
     *
     * @param linkable an object of type TLinkable
     * @return always true
     */
    public boolean add(T linkable) {
	insert(_size, linkable);
	return true;
    }

    /**
     * Inserts newElement into the list immediately before current.
     * All elements to the right of and including current are shifted
     * over.
     *
     * @param current    a &lt;code&gt;TLinkable&lt;/code&gt; value currently in the list.
     * @param newElement a &lt;code&gt;TLinkable&lt;/code&gt; value to be added to
     *                   the list.
     */
    public void addBefore(T current, T newElement) {
	if (current == _head) {
	    addFirst(newElement);
	} else if (current == null) {
	    addLast(newElement);
	} else {
	    T p = current.getPrevious();
	    newElement.setNext(current);
	    p.setNext(newElement);
	    newElement.setPrevious(p);
	    current.setPrevious(newElement);
	    _size++;
	}
    }

    /**
     * Implementation of index-based list insertions.
     *
     * @param index    an &lt;code&gt;int&lt;/code&gt; value
     * @param linkable an object of type TLinkable
     */
    @SuppressWarnings({ "unchecked" })
    protected void insert(int index, T linkable) {

	if (_size == 0) {
	    _head = _tail = linkable; // first insertion
	} else if (index == 0) {
	    linkable.setNext(_head); // insert at front
	    _head.setPrevious(linkable);
	    _head = linkable;
	} else if (index == _size) { // insert at back
	    _tail.setNext(linkable);
	    linkable.setPrevious(_tail);
	    _tail = linkable;
	} else {
	    T node = get(index);

	    T before = node.getPrevious();
	    if (before != null) {
		before.setNext(linkable);
	    }

	    linkable.setPrevious(before);
	    linkable.setNext(node);
	    node.setPrevious(linkable);
	}
	_size++;
    }

    /**
     * Inserts &lt;tt&gt;linkable&lt;/tt&gt; at the head of the list.
     *
     * @param linkable an object of type TLinkable
     */
    public void addFirst(T linkable) {
	insert(0, linkable);
    }

    /**
     * Adds &lt;tt&gt;linkable&lt;/tt&gt; to the end of the list.
     *
     * @param linkable an object of type TLinkable
     */
    public void addLast(T linkable) {
	insert(size(), linkable);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({ "unchecked" })
    public T get(int index) {
	// Blow out for bogus values
	if (index &lt; 0 || index &gt;= _size) {
	    throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + _size);
	}

	// Determine if it's better to get there from the front or the back
	if (index &gt; (_size &gt;&gt; 1)) {
	    int position = _size - 1;
	    T node = _tail;

	    while (position &gt; index) {
		node = node.getPrevious();
		position--;
	    }

	    return node;
	} else {
	    int position = 0;
	    T node = _head;

	    while (position &lt; index) {
		node = node.getNext();
		position++;
	    }

	    return node;
	}
    }

    /**
     * Returns the number of elements in the list.
     *
     * @return an &lt;code&gt;int&lt;/code&gt; value
     */
    public int size() {
	return _size;
    }

}

