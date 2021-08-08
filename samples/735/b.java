class TLinkedList&lt;T&gt; extends AbstractSequentialList&lt;T&gt; implements Externalizable {
    class IteratorImpl implements ListIterator&lt;T&gt; {
	/**
	 * Returns the previous element's index.
	 *
	 * @return an &lt;code&gt;int&lt;/code&gt; value
	 */
	public final int previousIndex() {
	    return _nextIndex - 1;
	}

	private int _nextIndex = 0;

    }

}

