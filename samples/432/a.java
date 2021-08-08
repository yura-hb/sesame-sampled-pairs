import java.util.concurrent.locks.ReentrantLock;

class ConcurrentHashMapV7&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    /**
     * Returns &lt;tt&gt;true&lt;/tt&gt; if this map maps one or more keys to the
     * specified value. Note: This method requires a full internal
     * traversal of the hash table, and so is much slower than
     * method &lt;tt&gt;containsKey&lt;/tt&gt;.
     *
     * @param value value whose presence in this map is to be tested
     * @return &lt;tt&gt;true&lt;/tt&gt; if this map maps one or more keys to the
     *         specified value
     * @throws NullPointerException if the specified value is null
     */
    @Override
    public boolean containsValue(Object value) {
	// Same idea as size()
	if (value == null) {
	    throw new NullPointerException();
	}
	final Segment&lt;K, V&gt;[] segments = this.segments;
	boolean found = false;
	long last = 0;
	int retries = -1;
	try {
	    outer: for (;;) {
		if (retries++ == RETRIES_BEFORE_LOCK) {
		    for (int j = 0; j &lt; segments.length; ++j) {
			ensureSegment(j).lock(); // force creation
		    }
		}
		long hashSum = 0L;
		int sum = 0;
		for (int j = 0; j &lt; segments.length; ++j) {
		    HashEntry&lt;K, V&gt;[] tab;
		    Segment&lt;K, V&gt; seg = segmentAt(segments, j);
		    if (seg != null && (tab = seg.table) != null) {
			for (int i = 0; i &lt; tab.length; i++) {
			    HashEntry&lt;K, V&gt; e;
			    for (e = entryAt(tab, i); e != null; e = e.next) {
				V v = e.value;
				if (v != null && value.equals(v)) {
				    found = true;
				    break outer;
				}
			    }
			}
			sum += seg.modCount;
		    }
		}
		if (retries &gt; 0 && sum == last) {
		    break;
		}
		last = sum;
	    }
	} finally {
	    if (retries &gt; RETRIES_BEFORE_LOCK) {
		for (int j = 0; j &lt; segments.length; ++j) {
		    segmentAt(segments, j).unlock();
		}
	    }
	}
	return found;
    }

    /**
     * The segments, each of which is a specialized hash table.
     */
    final Segment&lt;K, V&gt;[] segments;
    /**
     * Number of unsynchronized retries in size and containsValue
     * methods before resorting to locking. This is used to avoid
     * unbounded retries if tables undergo continuous modification
     * which would make it impossible to obtain an accurate result.
     */
    static final int RETRIES_BEFORE_LOCK = 2;
    private static final int SSHIFT;
    private static final long SBASE;
    private static final sun.misc.Unsafe UNSAFE;
    private static final int TSHIFT;
    private static final long TBASE;

    /**
     * Returns the segment for the given index, creating it and
     * recording in segment table (via CAS) if not already present.
     *
     * @param k the index
     * @return the segment
     */
    @SuppressWarnings("unchecked")
    private Segment&lt;K, V&gt; ensureSegment(int k) {
	final Segment&lt;K, V&gt;[] ss = this.segments;
	long u = (k &lt;&lt; SSHIFT) + SBASE; // raw offset
	Segment&lt;K, V&gt; seg;
	if ((seg = (Segment&lt;K, V&gt;) UNSAFE.getObjectVolatile(ss, u)) == null) {
	    Segment&lt;K, V&gt; proto = ss[0]; // use segment 0 as prototype
	    int cap = proto.table.length;
	    float lf = proto.loadFactor;
	    int threshold = (int) (cap * lf);
	    HashEntry&lt;K, V&gt;[] tab = new HashEntry[cap];
	    if ((seg = (Segment&lt;K, V&gt;) UNSAFE.getObjectVolatile(ss, u)) == null) { // recheck
		Segment&lt;K, V&gt; s = new Segment&lt;K, V&gt;(lf, threshold, tab);
		while ((seg = (Segment&lt;K, V&gt;) UNSAFE.getObjectVolatile(ss, u)) == null) {
		    if (UNSAFE.compareAndSwapObject(ss, u, null, seg = s)) {
			break;
		    }
		}
	    }
	}
	return seg;
    }

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
     * Gets the ith element of given table (if nonnull) with volatile
     * read semantics. Note: This is manually integrated into a few
     * performance-sensitive methods to reduce call overhead.
     */
    @SuppressWarnings("unchecked")
    static final &lt;K, V&gt; HashEntry&lt;K, V&gt; entryAt(HashEntry&lt;K, V&gt;[] tab, int i) {
	return (tab == null) ? null : (HashEntry&lt;K, V&gt;) UNSAFE.getObjectVolatile(tab, ((long) i &lt;&lt; TSHIFT) + TBASE);
    }

    class Segment&lt;K, V&gt; extends ReentrantLock implements Serializable {
	/**
	* The segments, each of which is a specialized hash table.
	*/
	final Segment&lt;K, V&gt;[] segments;
	/**
	* Number of unsynchronized retries in size and containsValue
	* methods before resorting to locking. This is used to avoid
	* unbounded retries if tables undergo continuous modification
	* which would make it impossible to obtain an accurate result.
	*/
	static final int RETRIES_BEFORE_LOCK = 2;
	private static final int SSHIFT;
	private static final long SBASE;
	private static final sun.misc.Unsafe UNSAFE;
	private static final int TSHIFT;
	private static final long TBASE;

	Segment(float lf, int threshold, HashEntry&lt;K, V&gt;[] tab) {
	    this.loadFactor = lf;
	    this.threshold = threshold;
	    this.table = tab;
	}

    }

}

