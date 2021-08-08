class ConcurrentHashMapV7&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * &lt;p&gt;More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code key.equals(k)},
     * then this method returns {@code v}; otherwise it returns
     * {@code null}.  (There can be at most one such mapping.)
     *
     * @throws NullPointerException if the specified key is null
     */
    @Override
    public V get(Object key) {
	Segment&lt;K, V&gt; s; // manually integrate access methods to reduce overhead
	HashEntry&lt;K, V&gt;[] tab;
	int h = hash(key);
	long u = (((h &gt;&gt;&gt; segmentShift) & segmentMask) &lt;&lt; SSHIFT) + SBASE;
	if ((s = (Segment&lt;K, V&gt;) UNSAFE.getObjectVolatile(segments, u)) != null && (tab = s.table) != null) {
	    for (HashEntry&lt;K, V&gt; e = (HashEntry&lt;K, V&gt;) UNSAFE.getObjectVolatile(tab,
		    ((long) (((tab.length - 1) & h)) &lt;&lt; TSHIFT) + TBASE); e != null; e = e.next) {
		K k;
		if ((k = e.key) == key || (e.hash == h && key.equals(k))) {
		    return e.value;
		}
	    }
	}
	return null;
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

