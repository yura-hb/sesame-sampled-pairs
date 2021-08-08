import jdk.internal.misc.Unsafe;

class ConcurrentHashMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
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
    public V get(Object key) {
	Node&lt;K, V&gt;[] tab;
	Node&lt;K, V&gt; e, p;
	int n, eh;
	K ek;
	int h = spread(key.hashCode());
	if ((tab = table) != null && (n = tab.length) &gt; 0 && (e = tabAt(tab, (n - 1) & h)) != null) {
	    if ((eh = e.hash) == h) {
		if ((ek = e.key) == key || (ek != null && key.equals(ek)))
		    return e.val;
	    } else if (eh &lt; 0)
		return (p = e.find(h, key)) != null ? p.val : null;
	    while ((e = e.next) != null) {
		if (e.hash == h && ((ek = e.key) == key || (ek != null && key.equals(ek))))
		    return e.val;
	    }
	}
	return null;
    }

    /**
     * The array of bins. Lazily initialized upon first insertion.
     * Size is always a power of two. Accessed directly by iterators.
     */
    transient volatile Node&lt;K, V&gt;[] table;
    static final int HASH_BITS = 0x7fffffff;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final int ASHIFT;
    private static final int ABASE;

    /**
     * Spreads (XORs) higher bits of hash to lower and also forces top
     * bit to 0. Because the table uses power-of-two masking, sets of
     * hashes that vary only in bits above the current mask will
     * always collide. (Among known examples are sets of Float keys
     * holding consecutive whole numbers in small tables.)  So we
     * apply a transform that spreads the impact of higher bits
     * downward. There is a tradeoff between speed, utility, and
     * quality of bit-spreading. Because many common sets of hashes
     * are already reasonably distributed (so don't benefit from
     * spreading), and because we use trees to handle large sets of
     * collisions in bins, we just XOR some shifted bits in the
     * cheapest possible way to reduce systematic lossage, as well as
     * to incorporate impact of the highest bits that would otherwise
     * never be used in index calculations because of table bounds.
     */
    static final int spread(int h) {
	return (h ^ (h &gt;&gt;&gt; 16)) & HASH_BITS;
    }

    @SuppressWarnings("unchecked")
    static final &lt;K, V&gt; Node&lt;K, V&gt; tabAt(Node&lt;K, V&gt;[] tab, int i) {
	return (Node&lt;K, V&gt;) U.getObjectAcquire(tab, ((long) i &lt;&lt; ASHIFT) + ABASE);
    }

    class Node&lt;K, V&gt; implements Entry&lt;K, V&gt; {
	/**
	* The array of bins. Lazily initialized upon first insertion.
	* Size is always a power of two. Accessed directly by iterators.
	*/
	transient volatile Node&lt;K, V&gt;[] table;
	static final int HASH_BITS = 0x7fffffff;
	private static final Unsafe U = Unsafe.getUnsafe();
	private static final int ASHIFT;
	private static final int ABASE;

	/**
	 * Virtualized support for map.get(); overridden in subclasses.
	 */
	Node&lt;K, V&gt; find(int h, Object k) {
	    Node&lt;K, V&gt; e = this;
	    if (k != null) {
		do {
		    K ek;
		    if (e.hash == h && ((ek = e.key) == k || (ek != null && k.equals(ek))))
			return e;
		} while ((e = e.next) != null);
	    }
	    return null;
	}

    }

}

