class CacheStats {
    /**
    * Returns a new {@code CacheStats} representing the difference between this {@code CacheStats}
    * and {@code other}. Negative values, which aren't supported by {@code CacheStats} will be
    * rounded up to zero.
    *
    * @param other the statistics to subtract with
    * @return the difference between this instance and {@code other}
    */
    @NonNull
    public CacheStats minus(@NonNull CacheStats other) {
	return new CacheStats(Math.max(0L, hitCount - other.hitCount), Math.max(0L, missCount - other.missCount),
		Math.max(0L, loadSuccessCount - other.loadSuccessCount),
		Math.max(0L, loadFailureCount - other.loadFailureCount),
		Math.max(0L, totalLoadTime - other.totalLoadTime), Math.max(0L, evictionCount - other.evictionCount),
		Math.max(0L, evictionWeight - other.evictionWeight));
    }

    private final long hitCount;
    private final long missCount;
    private final long loadSuccessCount;
    private final long loadFailureCount;
    private final long totalLoadTime;
    private final long evictionCount;
    private final long evictionWeight;

    /**
    * Constructs a new {@code CacheStats} instance.
    * &lt;p&gt;
    * Many parameters of the same type in a row is a bad thing, but this class is not constructed
    * by end users and is too fine-grained for a builder.
    *
    * @param hitCount the number of cache hits
    * @param missCount the number of cache misses
    * @param loadSuccessCount the number of successful cache loads
    * @param loadFailureCount the number of failed cache loads
    * @param totalLoadTime the total load time (success and failure)
    * @param evictionCount the number of entries evicted from the cache
    * @param evictionWeight the sum of weights of entries evicted from the cache
    */
    public CacheStats(@NonNegative long hitCount, @NonNegative long missCount, @NonNegative long loadSuccessCount,
	    @NonNegative long loadFailureCount, @NonNegative long totalLoadTime, @NonNegative long evictionCount,
	    @NonNegative long evictionWeight) {
	if ((hitCount &lt; 0) || (missCount &lt; 0) || (loadSuccessCount &lt; 0) || (loadFailureCount &lt; 0) || (totalLoadTime &lt; 0)
		|| (evictionCount &lt; 0) || (evictionWeight &lt; 0)) {
	    throw new IllegalArgumentException();
	}
	this.hitCount = hitCount;
	this.missCount = missCount;
	this.loadSuccessCount = loadSuccessCount;
	this.loadFailureCount = loadFailureCount;
	this.totalLoadTime = totalLoadTime;
	this.evictionCount = evictionCount;
	this.evictionWeight = evictionWeight;
    }

}

