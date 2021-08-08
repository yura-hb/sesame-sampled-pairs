class Caffeine&lt;K, V&gt; {
    /**
    * Specifies the weigher to use in determining the weight of entries. Entry weight is taken into
    * consideration by {@link #maximumWeight(long)} when determining which entries to evict, and use
    * of this method requires a corresponding call to {@link #maximumWeight(long)} prior to calling
    * {@link #build}. Weights are measured and recorded when entries are inserted into or updated in
    * the cache, and are thus effectively static during the lifetime of a cache entry.
    * &lt;p&gt;
    * When the weight of an entry is zero it will not be considered for size-based eviction (though
    * it still may be evicted by other means).
    * &lt;p&gt;
    * &lt;b&gt;Important note:&lt;/b&gt; Instead of returning &lt;em&gt;this&lt;/em&gt; as a {@code Caffeine} instance, this
    * method returns {@code Caffeine&lt;K1, V1&gt;}. From this point on, either the original reference or
    * the returned reference may be used to complete configuration and build the cache, but only the
    * "generic" one is type-safe. That is, it will properly prevent you from building caches whose
    * key or value types are incompatible with the types accepted by the weigher already provided;
    * the {@code Caffeine} type cannot do this. For best results, simply use the standard
    * method-chaining idiom, as illustrated in the documentation at top, configuring a
    * {@code Caffeine} and building your {@link Cache} all in a single statement.
    * &lt;p&gt;
    * &lt;b&gt;Warning:&lt;/b&gt; if you ignore the above advice, and use this {@code Caffeine} to build a cache
    * whose key or value type is incompatible with the weigher, you will likely experience a
    * {@link ClassCastException} at some &lt;i&gt;undefined&lt;/i&gt; point in the future.
    *
    * @param weigher the weigher to use in calculating the weight of cache entries
    * @param &lt;K1&gt; key type of the weigher
    * @param &lt;V1&gt; value type of the weigher
    * @return the cache builder reference that should be used instead of {@code this} for any
    *         remaining configuration and cache building
    * @throws IllegalStateException if a weigher was already set
    */
    @NonNull
    public &lt;K1 extends K, V1 extends V&gt; Caffeine&lt;K1, V1&gt; weigher(@NonNull Weigher&lt;? super K1, ? super V1&gt; weigher) {
	requireNonNull(weigher);
	requireState(this.weigher == null, "weigher was already set to %s", this.weigher);
	requireState(!strictParsing || this.maximumSize == UNSET_INT, "weigher can not be combined with maximum size",
		this.maximumSize);

	@SuppressWarnings("unchecked")
	Caffeine&lt;K1, V1&gt; self = (Caffeine&lt;K1, V1&gt;) this;
	self.weigher = weigher;
	return self;
    }

    @Nullable
    Weigher&lt;? super K, ? super V&gt; weigher;
    boolean strictParsing = true;
    long maximumSize = UNSET_INT;
    static final int UNSET_INT = -1;

    /** Ensures that the state expression is true. */
    static void requireState(boolean expression, String template, Object... args) {
	if (!expression) {
	    throw new IllegalStateException(String.format(template, args));
	}
    }

}

