import java.util.Queue;

class IteratorChain&lt;E&gt; implements Iterator&lt;E&gt; {
    /**
     * Add an Iterator to the end of the chain
     *
     * @param iterator Iterator to add
     * @throws IllegalStateException if I've already started iterating
     * @throws NullPointerException if the iterator is null
     */
    public void addIterator(final Iterator&lt;? extends E&gt; iterator) {
	checkLocked();
	if (iterator == null) {
	    throw new NullPointerException("Iterator must not be null");
	}
	iteratorChain.add(iterator);
    }

    /** The chain of iterators */
    private final Queue&lt;Iterator&lt;? extends E&gt;&gt; iteratorChain = new LinkedList&lt;&gt;();
    /**
     * ComparatorChain is "locked" after the first time compare(Object,Object)
     * is called
     */
    private boolean isLocked = false;

    /**
     * Checks whether the iterator chain is now locked and in use.
     */
    private void checkLocked() {
	if (isLocked == true) {
	    throw new UnsupportedOperationException(
		    "IteratorChain cannot be changed after the first use of a method from the Iterator interface");
	}
    }

}

