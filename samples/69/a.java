class Caffeine&lt;K, V&gt; {
    /**
    * Specifies the maximum weight of entries the cache may contain. Weight is determined using the
    * {@link Weigher} specified with {@link #weigher}, and use of this method requires a
    * corresponding call to {@link #weigher} prior to calling {@link #build}.
    * &lt;p&gt;
    * Note that the cache &lt;b&gt;may evict an entry before this limit is exceeded or temporarily exceed
    * the threshold while evicting&lt;/b&gt;. As the cache size grows close to the maximum, the cache
    * evicts entries that are less likely to be used again. For example, the cache may evict an entry
    * because it hasn't been used recently or very often.
    * &lt;p&gt;
    * When {@code maximumWeight} is zero, elements will be evicted immediately after being loaded
    * into cache. This can be useful in testing, or to disable caching temporarily without a code
    * change.
    * &lt;p&gt;
    * Note that weight is only used to determine whether the cache is over capacity; it has no effect
    * on selecting which entry should be evicted next.
    * &lt;p&gt;
    * This feature cannot be used in conjunction with {@link #maximumSize}.
    *
    * @param maximumWeight the maximum total weight of entries the cache may contain
    * @return this {@code Caffeine} instance (for chaining)
    * @throws IllegalArgumentException if {@code maximumWeight} is negative
    * @throws IllegalStateException if a maximum weight or size was already set
    */
    @NonNull
    public Caffeine&lt;K, V&gt; maximumWeight(@NonNegative long maximumWeight) {
	requireState(this.maximumWeight == UNSET_INT, "maximum weight was already set to %s", this.maximumWeight);
	requireState(this.maximumSize == UNSET_INT, "maximum size was already set to %s", this.maximumSize);
	this.maximumWeight = maximumWeight;
	requireArgument(maximumWeight &gt;= 0, "maximum weight must not be negative");
	return this;
    }

    long maximumWeight = UNSET_INT;
    static final int UNSET_INT = -1;
    long maximumSize = UNSET_INT;

    /** Ensures that the state expression is true. */
    static void requireState(boolean expression, String template, Object... args) {
	if (!expression) {
	    throw new IllegalStateException(String.format(template, args));
	}
    }

    /** Ensures that the argument expression is true. */
    static void requireArgument(boolean expression, String template, Object... args) {
	if (!expression) {
	    throw new IllegalArgumentException(String.format(template, args));
	}
    }

}

