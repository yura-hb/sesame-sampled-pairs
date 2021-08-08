class CacheBuilder&lt;K, V&gt; {
    /**
    * Specifies a nanosecond-precision time source for this cache. By default, {@link
    * System#nanoTime} is used.
    *
    * &lt;p&gt;The primary intent of this method is to facilitate testing of caches with a fake or mock
    * time source.
    *
    * @return this {@code CacheBuilder} instance (for chaining)
    * @throws IllegalStateException if a ticker was already set
    */
    public CacheBuilder&lt;K, V&gt; ticker(Ticker ticker) {
	checkState(this.ticker == null);
	this.ticker = checkNotNull(ticker);
	return this;
    }

    @MonotonicNonNull
    Ticker ticker;

}

