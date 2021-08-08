import gnu.trove.list.TLinkable;

class TLinkedList&lt;T&gt; extends AbstractSequentialList&lt;T&gt; implements Externalizable {
    /**
     * Remove and return the first element in the list.
     *
     * @return an &lt;code&gt;Object&lt;/code&gt; value
     */
    @SuppressWarnings({ "unchecked" })
    public T removeFirst() {
	T o = _head;

	if (o == null) {
	    return null;
	}

	T n = o.getNext();
	o.setNext(null);

	if (null != n) {
	    n.setPrevious(null);
	}

	_head = n;
	if (--_size == 0) {
	    _tail = null;
	}
	return o;
    }

    /** the head of the list */
    protected T _head;
    /** the number of elements in the list */
    protected int _size = 0;
    /** the tail of the list */
    protected T _tail;

}

