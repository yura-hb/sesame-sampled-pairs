import java.util.concurrent.locks.ReentrantLock;

class ConcurrentHashMapV7&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    /**
     * Removes all of the mappings from this map.
     */
    @Override
    public void clear() {
	final Segment&lt;K, V&gt;[] segments = this.segments;
	for (int j = 0; j &lt; segments.length; ++j) {
	    Segment&lt;K, V&gt; s = segmentAt(segments, j);
	    if (s != null) {
		s.clear();
	    }
	}
    }

    /**
     * The segments, each of which is a specialized hash table.
     */
    final Segment&lt;K, V&gt;[] segments;
    private static final int SSHIFT;
    private static final long SBASE;
    private static final sun.misc.Unsafe UNSAFE;
    private static final int TSHIFT;
    private static final long TBASE;

    /**
     * Gets the jth element of given segment array (if nonnull) with
     * volatile element access semantics via Unsafe. (The null check
     * can trigger harmlessly only during deserialization.) Note:
     * because each element of segments array is set only once (using
     * fully ordered writes), some performance-sensitive methods rely
     * on this method only as a recheck upon null reads.
     */
    @SuppressWarnings("unchecked")
    static final &lt;K, V&gt; Segment&lt;K, V&gt; segmentAt(Segment&lt;K, V&gt;[] ss, int j) {
	long u = (j &lt;&lt; SSHIFT) + SBASE;
	return ss == null ? null : (Segment&lt;K, V&gt;) UNSAFE.getObjectVolatile(ss, u);
    }

    /**
     * Sets the ith element of given table, with volatile write
     * semantics. (See above about use of putOrderedObject.)
     */
    static final &lt;K, V&gt; void setEntryAt(HashEntry&lt;K, V&gt;[] tab, int i, HashEntry&lt;K, V&gt; e) {
	UNSAFE.putOrderedObject(tab, ((long) i &lt;&lt; TSHIFT) + TBASE, e);
    }

    class Segment&lt;K, V&gt; extends ReentrantLock implements Serializable {
	/**
	* The segments, each of which is a specialized hash table.
	*/
	final Segment&lt;K, V&gt;[] segments;
	private static final int SSHIFT;
	private static final long SBASE;
	private static final sun.misc.Unsafe UNSAFE;
	private static final int TSHIFT;
	private static final long TBASE;

	final void clear() {
	    lock();
	    try {
		HashEntry&lt;K, V&gt;[] tab = table;
		for (int i = 0; i &lt; tab.length; i++) {
		    setEntryAt(tab, i, null);
		}
		++modCount;
		count = 0;
	    } finally {
		unlock();
	    }
	}

    }

}

