import gnu.trove.list.TLinkable;

class TLinkedList&lt;T&gt; extends AbstractSequentialList&lt;T&gt; implements Externalizable {
    /** Empties the list. */
    public void clear() {
	if (null != _head) {
	    for (TLinkable&lt;T&gt; link = _head.getNext(); link != null; link = link.getNext()) {
		TLinkable&lt;T&gt; prev = link.getPrevious();
		prev.setNext(null);
		link.setPrevious(null);
	    }
	    _head = _tail = null;
	}
	_size = 0;
    }

    /** the head of the list */
    protected T _head;
    /** the tail of the list */
    protected T _tail;
    /** the number of elements in the list */
    protected int _size = 0;

}

