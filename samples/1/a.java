class ConcurrentHashMapV7&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    /**
     * Returns &lt;tt&gt;true&lt;/tt&gt; if this map contains no key-value mappings.
     *
     * @return &lt;tt&gt;true&lt;/tt&gt; if this map contains no key-value mappings
     */
    @Override
    public boolean isEmpty() {
	/*
	 * Sum per-segment modCounts to avoid mis-reporting when
	 * elements are concurrently added and removed in one segment
	 * while checking another, in which case the table was never
	 * actually empty at any point. (The sum ensures accuracy up
	 * through at least 1&lt;&lt;31 per-segment modifications before
	 * recheck.)  Methods size() and containsValue() use similar
	 * constructions for stability checks.
	 */
	long sum = 0L;
	final Segment&lt;K, V&gt;[] segments = this.segments;
	for (int j = 0; j &lt; segments.length; ++j) {
	    Segment&lt;K, V&gt; seg = segmentAt(segments, j);
	    if (seg != null) {
		if (seg.count != 0) {
		    return false;
		}
		sum += seg.modCount;
	    }
	}
	if (sum != 0L) { // recheck unless no modifications
	    for (int j = 0; j &lt; segments.length; ++j) {
		Segment&lt;K, V&gt; seg = segmentAt(segments, j);
		if (seg != null) {
		    if (seg.count != 0) {
			return false;
		    }
		    sum -= seg.modCount;
		}
	    }
	    if (sum != 0L) {
		return false;
	    }
	}
	return true;
    }

    /**
     * The segments, each of which is a specialized hash table.
     */
    final Segment&lt;K, V&gt;[] segments;
    private static final int SSHIFT;
    private static final long SBASE;
    private static final sun.misc.Unsafe UNSAFE;

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

}

