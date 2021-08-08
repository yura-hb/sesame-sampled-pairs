class CacheBuilder&lt;K, V&gt; {
    /**
    * Specifies the maximum weight of entries the cache may contain. Weight is determined using the
    * {@link Weigher} specified with {@link #weigher}, and use of this method requires a
    * corresponding call to {@link #weigher} prior to calling {@link #build}.
    *
    * &lt;p&gt;Note that the cache &lt;b&gt;may evict an entry before this limit is exceeded&lt;/b&gt;. For example, in
    * the current implementation, when {@code concurrencyLevel} is greater than {@code 1}, each
    * resulting segment inside the cache &lt;i&gt;independently&lt;/i&gt; limits its own weight to approximately
    * {@code maximumWeight / concurrencyLevel}.
    *
    * &lt;p&gt;When eviction is necessary, the cache evicts entries that are less likely to be used again.
    * For example, the cache may evict an entry because it hasn't been used recently or very often.
    *
    * &lt;p&gt;If {@code maximumWeight} is zero, elements will be evicted immediately after being loaded
    * into cache. This can be useful in testing, or to disable caching temporarily.
    *
    * &lt;p&gt;Note that weight is only used to determine whether the cache is over capacity; it has no
    * effect on selecting which entry should be evicted next.
    *
    * &lt;p&gt;This feature cannot be used in conjunction with {@link #maximumSize}.
    *
    * @param maximumWeight the maximum total weight of entries the cache may contain
    * @return this {@code CacheBuilder} instance (for chaining)
    * @throws IllegalArgumentException if {@code maximumWeight} is negative
    * @throws IllegalStateException if a maximum weight or size was already set
    * @since 11.0
    */
    @GwtIncompatible // To be supported
    public CacheBuilder&lt;K, V&gt; maximumWeight(long maximumWeight) {
	checkState(this.maximumWeight == UNSET_INT, "maximum weight was already set to %s", this.maximumWeight);
	checkState(this.maximumSize == UNSET_INT, "maximum size was already set to %s", this.maximumSize);
	this.maximumWeight = maximumWeight;
	checkArgument(maximumWeight &gt;= 0, "maximum weight must not be negative");
	return this;
    }

    long maximumWeight = UNSET_INT;
    static final int UNSET_INT = -1;
    long maximumSize = UNSET_INT;

}

