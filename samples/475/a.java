import java.util.Iterator;

class CursorableLinkedList&lt;E&gt; extends AbstractLinkedList&lt;E&gt; implements Serializable {
    /**
     * Removes all nodes by iteration.
     */
    @Override
    protected void removeAllNodes() {
	if (size() &gt; 0) {
	    // superclass implementation would break all the iterators
	    final Iterator&lt;E&gt; it = iterator();
	    while (it.hasNext()) {
		it.next();
		it.remove();
	    }
	}
    }

    /**
     * Returns an iterator that does &lt;b&gt;not&lt;/b&gt; support concurrent modification.
     * &lt;p&gt;
     * If the underlying list is modified while iterating using this iterator
     * a ConcurrentModificationException will occur.
     * The cursor behaviour is available via {@link #listIterator()}.
     *
     * @return a new iterator that does &lt;b&gt;not&lt;/b&gt; support concurrent modification
     */
    @Override
    public Iterator&lt;E&gt; iterator() {
	return super.listIterator(0);
    }

}

