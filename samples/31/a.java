class ConcurrentHashMapV7&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    /**
     * Tests if the specified object is a key in this table.
     *
     * @param  key   possible key
     * @return &lt;tt&gt;true&lt;/tt&gt; if and only if the specified object
     *         is a key in this table, as determined by the
     *         &lt;tt&gt;equals&lt;/tt&gt; method; &lt;tt&gt;false&lt;/tt&gt; otherwise.
     * @throws NullPointerException if the specified key is null
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(Object key) {
	Segment&lt;K, V&gt; s; // same as get() except no need for volatile value read
	HashEntry&lt;K, V&gt;[] tab;
	int h = hash(key);
	long u = (((h &gt;&gt;&gt; segmentShift) & segmentMask) &lt;&lt; SSHIFT) + SBASE;
	if ((s = (Segment&lt;K, V&gt;) UNSAFE.getObjectVolatile(segments, u)) != null && (tab = s.table) != null) {
	    for (HashEntry&lt;K, V&gt; e = (HashEntry&lt;K, V&gt;) UNSAFE.getObjectVolatile(tab,
		    ((long) (((tab.length - 1) & h)) &lt;&lt; TSHIFT) + TBASE); e != null; e = e.next) {
		K k;
		if ((k = e.key) == key || (e.hash == h && key.equals(k))) {
		    return true;
		}
	    }
	}
	return false;
    }

    /**
     * Shift value for indexing within segments.
     */
    final int segmentShift;
    /**
     * Mask value for indexing into segments. The upper bits of a
     * key's hash code are used to choose the segment.
     */
    final int segmentMask;
    private static final int SSHIFT;
    private static final long SBASE;
    private static final sun.misc.Unsafe UNSAFE;
    /**
     * The segments, each of which is a specialized hash table.
     */
    final Segment&lt;K, V&gt;[] segments;
    private static final int TSHIFT;
    private static final long TBASE;
    /**
     * A randomizing value associated with this instance that is applied to
     * hash code of keys to make hash collisions harder to find.
     */
    private transient final int hashSeed = randomHashSeed(this);

    /**
     * Applies a supplemental hash function to a given hashCode, which
     * defends against poor quality hash functions.  This is critical
     * because ConcurrentHashMap uses power-of-two length hash tables,
     * that otherwise encounter collisions for hashCodes that do not
     * differ in lower or upper bits.
     */
    private int hash(Object k) {
	int h = hashSeed;

	//        if ((0 != h) && (k instanceof String)) {
	//            return sun.misc.Hashing.stringHash32((String) k);
	//        }

	h ^= k.hashCode();

	// Spread bits to regularize both segment and index locations,
	// using variant of single-word Wang/Jenkins hash.
	h += (h &lt;&lt; 15) ^ 0xffffcd7d;
	h ^= (h &gt;&gt;&gt; 10);
	h += (h &lt;&lt; 3);
	h ^= (h &gt;&gt;&gt; 6);
	h += (h &lt;&lt; 2) + (h &lt;&lt; 14);
	return h ^ (h &gt;&gt;&gt; 16);
    }

}

