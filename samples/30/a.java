class Caffeine&lt;K, V&gt; {
    /**
    * Specifies a listener instance that caches should notify each time an entry is removed for any
    * {@linkplain RemovalCause reason}. Each cache created by this builder will invoke this listener
    * as part of the routine maintenance described in the class documentation above.
    * &lt;p&gt;
    * &lt;b&gt;Warning:&lt;/b&gt; after invoking this method, do not continue to use &lt;i&gt;this&lt;/i&gt; cache builder
    * reference; instead use the reference this method &lt;i&gt;returns&lt;/i&gt;. At runtime, these point to the
    * same instance, but only the returned reference has the correct generic type information so as
    * to ensure type safety. For best results, use the standard method-chaining idiom illustrated in
    * the class documentation above, configuring a builder and building your cache in a single
    * statement. Failure to heed this advice can result in a {@link ClassCastException} being thrown
    * by a cache operation at some &lt;i&gt;undefined&lt;/i&gt; point in the future.
    * &lt;p&gt;
    * &lt;b&gt;Warning:&lt;/b&gt; any exception thrown by {@code listener} will &lt;i&gt;not&lt;/i&gt; be propagated to the
    * {@code Cache} user, only logged via a {@link Logger}.
    *
    * @param removalListener a listener instance that caches should notify each time an entry is
    *        removed
    * @param &lt;K1&gt; the key type of the listener
    * @param &lt;V1&gt; the value type of the listener
    * @return the cache builder reference that should be used instead of {@code this} for any
    *         remaining configuration and cache building
    * @throws IllegalStateException if a removal listener was already set
    * @throws NullPointerException if the specified removal listener is null
    */
    @NonNull
    public &lt;K1 extends K, V1 extends V&gt; Caffeine&lt;K1, V1&gt; removalListener(
	    @NonNull RemovalListener&lt;? super K1, ? super V1&gt; removalListener) {
	requireState(this.removalListener == null, "removal listener was already set to %s", this.removalListener);

	@SuppressWarnings("unchecked")
	Caffeine&lt;K1, V1&gt; self = (Caffeine&lt;K1, V1&gt;) this;
	self.removalListener = requireNonNull(removalListener);
	return self;
    }

    @Nullable
    RemovalListener&lt;? super K, ? super V&gt; removalListener;

    /** Ensures that the state expression is true. */
    static void requireState(boolean expression, String template, Object... args) {
	if (!expression) {
	    throw new IllegalStateException(String.format(template, args));
	}
    }

}

