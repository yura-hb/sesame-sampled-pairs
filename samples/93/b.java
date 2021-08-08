class CacheBuilder&lt;K, V&gt; {
    /**
    * Specifies the weigher to use in determining the weight of entries. Entry weight is taken into
    * consideration by {@link #maximumWeight(long)} when determining which entries to evict, and use
    * of this method requires a corresponding call to {@link #maximumWeight(long)} prior to calling
    * {@link #build}. Weights are measured and recorded when entries are inserted into the cache, and
    * are thus effectively static during the lifetime of a cache entry.
    *
    * &lt;p&gt;When the weight of an entry is zero it will not be considered for size-based eviction
    * (though it still may be evicted by other means).
    *
    * &lt;p&gt;&lt;b&gt;Important note:&lt;/b&gt; Instead of returning &lt;em&gt;this&lt;/em&gt; as a {@code CacheBuilder}
    * instance, this method returns {@code CacheBuilder&lt;K1, V1&gt;}. From this point on, either the
    * original reference or the returned reference may be used to complete configuration and build
    * the cache, but only the "generic" one is type-safe. That is, it will properly prevent you from
    * building caches whose key or value types are incompatible with the types accepted by the
    * weigher already provided; the {@code CacheBuilder} type cannot do this. For best results,
    * simply use the standard method-chaining idiom, as illustrated in the documentation at top,
    * configuring a {@code CacheBuilder} and building your {@link Cache} all in a single statement.
    *
    * &lt;p&gt;&lt;b&gt;Warning:&lt;/b&gt; if you ignore the above advice, and use this {@code CacheBuilder} to build a
    * cache whose key or value type is incompatible with the weigher, you will likely experience a
    * {@link ClassCastException} at some &lt;i&gt;undefined&lt;/i&gt; point in the future.
    *
    * @param weigher the weigher to use in calculating the weight of cache entries
    * @return this {@code CacheBuilder} instance (for chaining)
    * @throws IllegalArgumentException if {@code size} is negative
    * @throws IllegalStateException if a maximum size was already set
    * @since 11.0
    */
    @GwtIncompatible // To be supported
    public &lt;K1 extends K, V1 extends V&gt; CacheBuilder&lt;K1, V1&gt; weigher(Weigher&lt;? super K1, ? super V1&gt; weigher) {
	checkState(this.weigher == null);
	if (strictParsing) {
	    checkState(this.maximumSize == UNSET_INT, "weigher can not be combined with maximum size",
		    this.maximumSize);
	}

	// safely limiting the kinds of caches this can produce
	@SuppressWarnings("unchecked")
	CacheBuilder&lt;K1, V1&gt; me = (CacheBuilder&lt;K1, V1&gt;) this;
	me.weigher = checkNotNull(weigher);
	return me;
    }

    @MonotonicNonNull
    Weigher&lt;? super K, ? super V&gt; weigher;
    boolean strictParsing = true;
    long maximumSize = UNSET_INT;
    static final int UNSET_INT = -1;

}

