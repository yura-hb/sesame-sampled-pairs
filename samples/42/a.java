import java.util.concurrent.locks.ReentrantLock;

class ConcurrentHashMapV7&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    /**
     * Returns the number of key-value mappings in this map.  If the
     * map contains more than &lt;tt&gt;Integer.MAX_VALUE&lt;/tt&gt; elements, returns
     * &lt;tt&gt;Integer.MAX_VALUE&lt;/tt&gt;.
     *
     * @return the number of key-value mappings in this map
     */
    @Override
    public int size() {
	// Try a few times to get accurate count. On failure due to
	// continuous async changes in table, resort to locking.
	final Segment&lt;K, V&gt;[] segments = this.segments;
	int size;
	boolean overflow; // true if size overflows 32 bits
	long sum; // sum of modCounts
	long last = 0L; // previous sum
	int retries = -1; // first iteration isn't retry
	try {
	    for (;;) {
		if (retries++ == RETRIES_BEFORE_LOCK) {
		    for (int j = 0; j &lt; segments.length; ++j) {
			ensureSegment(j).lock(); // force creation
		    }
		}
		sum = 0L;
		size = 0;
		overflow = false;
		for (int j = 0; j &lt; segments.length; ++j) {
		    Segment&lt;K, V&gt; seg = segmentAt(segments, j);
		    if (seg != null) {
			sum += seg.modCount;
			int c = seg.count;
			if (c &lt; 0 || (size += c) &lt; 0) {
			    overflow = true;
			}
		    }
		}
		if (sum == last) {
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
	return overflow ? Integer.MAX_VALUE : size;
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

	Segment(float lf, int threshold, HashEntry&lt;K, V&gt;[] tab) {
	    this.loadFactor = lf;
	    this.threshold = threshold;
	    this.table = tab;
	}

    }

}

