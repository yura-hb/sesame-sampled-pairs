class TLinkedList&lt;T&gt; extends AbstractSequentialList&lt;T&gt; implements Externalizable {
    class IteratorImpl implements ListIterator&lt;T&gt; {
	/**
	 * True if a call to next() will return an object.
	 *
	 * @return a &lt;code&gt;boolean&lt;/code&gt; value
	 */
	public final boolean hasNext() {
	    return _nextIndex != _size;
	}

	private int _nextIndex = 0;

    }

    /** the number of elements in the list */
    protected int _size = 0;

}

