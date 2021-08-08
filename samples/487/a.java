abstract class AbstractLinkedDeque&lt;E&gt; extends AbstractCollection&lt;E&gt; implements LinkedDeque&lt;E&gt; {
    /**
    * {@inheritDoc}
    * &lt;p&gt;
    * Beware that, unlike in most collections, this method is &lt;em&gt;NOT&lt;/em&gt; a constant-time operation.
    */
    @Override
    public int size() {
	int size = 0;
	for (E e = first; e != null; e = getNext(e)) {
	    size++;
	}
	return size;
    }

    /**
    * Pointer to first node.
    * Invariant: (first == null && last == null) ||
    *            (first.prev == null)
    */
    @Nullable
    E first;

}

