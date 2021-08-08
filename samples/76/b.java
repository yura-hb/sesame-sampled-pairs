class WeakHashMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements Map&lt;K, V&gt; {
    /**
     * Retrieve object hash code and applies a supplemental hash function to the
     * result hash, which defends against poor quality hash functions.  This is
     * critical because HashMap uses power-of-two length hash tables, that
     * otherwise encounter collisions for hashCodes that do not differ
     * in lower bits.
     */
    final int hash(Object k) {
	int h = k.hashCode();

	// This function ensures that hashCodes that differ only by
	// constant multiples at each bit position have a bounded
	// number of collisions (approximately 8 at default load factor).
	h ^= (h &gt;&gt;&gt; 20) ^ (h &gt;&gt;&gt; 12);
	return h ^ (h &gt;&gt;&gt; 7) ^ (h &gt;&gt;&gt; 4);
    }

}

