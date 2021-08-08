class CacheBuilder&lt;K, V&gt; {
    /**
    * Specifies a listener instance that caches should notify each time an entry is removed for any
    * {@linkplain RemovalCause reason}. Each cache created by this builder will invoke this listener
    * as part of the routine maintenance described in the class documentation above.
    *
    * &lt;p&gt;&lt;b&gt;Warning:&lt;/b&gt; after invoking this method, do not continue to use &lt;i&gt;this&lt;/i&gt; cache builder
    * reference; instead use the reference this method &lt;i&gt;returns&lt;/i&gt;. At runtime, these point to the
    * same instance, but only the returned reference has the correct generic type information so as
    * to ensure type safety. For best results, use the standard method-chaining idiom illustrated in
    * the class documentation above, configuring a builder and building your cache in a single
    * statement. Failure to heed this advice can result in a {@link ClassCastException} being thrown
    * by a cache operation at some &lt;i&gt;undefined&lt;/i&gt; point in the future.
    *
    * &lt;p&gt;&lt;b&gt;Warning:&lt;/b&gt; any exception thrown by {@code listener} will &lt;i&gt;not&lt;/i&gt; be propagated to
    * the {@code Cache} user, only logged via a {@link Logger}.
    *
    * @return the cache builder reference that should be used instead of {@code this} for any
    *     remaining configuration and cache building
    * @return this {@code CacheBuilder} instance (for chaining)
    * @throws IllegalStateException if a removal listener was already set
    */
    @CheckReturnValue
    public &lt;K1 extends K, V1 extends V&gt; CacheBuilder&lt;K1, V1&gt; removalListener(
	    RemovalListener&lt;? super K1, ? super V1&gt; listener) {
	checkState(this.removalListener == null);

	// safely limiting the kinds of caches this can produce
	@SuppressWarnings("unchecked")
	CacheBuilder&lt;K1, V1&gt; me = (CacheBuilder&lt;K1, V1&gt;) this;
	me.removalListener = checkNotNull(listener);
	return me;
    }

    @MonotonicNonNull
    RemovalListener&lt;? super K, ? super V&gt; removalListener;

}

