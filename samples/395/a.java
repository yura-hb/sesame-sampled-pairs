import java.util.BitSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

class ComparatorChain&lt;E&gt; implements Comparator&lt;E&gt;, Serializable {
    /**
     * Perform comparisons on the Objects as per
     * Comparator.compare(o1,o2).
     *
     * @param o1  the first object to compare
     * @param o2  the second object to compare
     * @return -1, 0, or 1
     * @throws UnsupportedOperationException if the ComparatorChain does not contain at least one Comparator
     */
    @Override
    public int compare(final E o1, final E o2) throws UnsupportedOperationException {
	if (isLocked == false) {
	    checkChainIntegrity();
	    isLocked = true;
	}

	// iterate over all comparators in the chain
	final Iterator&lt;Comparator&lt;E&gt;&gt; comparators = comparatorChain.iterator();
	for (int comparatorIndex = 0; comparators.hasNext(); ++comparatorIndex) {

	    final Comparator&lt;? super E&gt; comparator = comparators.next();
	    int retval = comparator.compare(o1, o2);
	    if (retval != 0) {
		// invert the order if it is a reverse sort
		if (orderingBits.get(comparatorIndex) == true) {
		    if (retval &gt; 0) {
			retval = -1;
		    } else {
			retval = 1;
		    }
		}
		return retval;
	    }
	}

	// if comparators are exhausted, return 0
	return 0;
    }

    /** Whether the chain has been "locked". */
    private boolean isLocked = false;
    /** The list of comparators in the chain. */
    private final List&lt;Comparator&lt;E&gt;&gt; comparatorChain;
    /** Order - false (clear) = ascend; true (set) = descend. */
    private BitSet orderingBits = null;

    /**
     * Throws an exception if the {@link ComparatorChain} is empty.
     *
     * @throws UnsupportedOperationException if the {@link ComparatorChain} is empty
     */
    private void checkChainIntegrity() {
	if (comparatorChain.size() == 0) {
	    throw new UnsupportedOperationException("ComparatorChains must contain at least one Comparator");
	}
    }

}

