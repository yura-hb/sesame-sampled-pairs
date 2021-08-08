import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

class IteratorChain&lt;E&gt; implements Iterator&lt;E&gt; {
    /**
     * Updates the current iterator field to ensure that the current Iterator is
     * not exhausted
     */
    protected void updateCurrentIterator() {
	if (currentIterator == null) {
	    if (iteratorChain.isEmpty()) {
		currentIterator = EmptyIterator.&lt;E&gt;emptyIterator();
	    } else {
		currentIterator = iteratorChain.remove();
	    }
	    // set last used iterator here, in case the user calls remove
	    // before calling hasNext() or next() (although they shouldn't)
	    lastUsedIterator = currentIterator;
	}

	while (currentIterator.hasNext() == false && !iteratorChain.isEmpty()) {
	    currentIterator = iteratorChain.remove();
	}
    }

    /** The current iterator */
    private Iterator&lt;? extends E&gt; currentIterator = null;
    /** The chain of iterators */
    private final Queue&lt;Iterator&lt;? extends E&gt;&gt; iteratorChain = new LinkedList&lt;&gt;();
    /**
     * The "last used" Iterator is the Iterator upon which next() or hasNext()
     * was most recently called used for the remove() operation only
     */
    private Iterator&lt;? extends E&gt; lastUsedIterator = null;

}

