class CacheStats {
    /**
    * Returns a new {@code CacheStats} representing the difference between this {@code CacheStats}
    * and {@code other}. Negative values, which aren't supported by {@code CacheStats} will be
    * rounded up to zero.
    */
    public CacheStats minus(CacheStats other) {
	return new CacheStats(Math.max(0, hitCount - other.hitCount), Math.max(0, missCount - other.missCount),
		Math.max(0, loadSuccessCount - other.loadSuccessCount),
		Math.max(0, loadExceptionCount - other.loadExceptionCount),
		Math.max(0, totalLoadTime - other.totalLoadTime), Math.max(0, evictionCount - other.evictionCount));
    }

    private final long hitCount;
    private final long missCount;
    private final long loadSuccessCount;
    private final long loadExceptionCount;
    private final long totalLoadTime;
    private final long evictionCount;

    /**
    * Constructs a new {@code CacheStats} instance.
    *
    * &lt;p&gt;Five parameters of the same type in a row is a bad thing, but this class is not constructed
    * by end users and is too fine-grained for a builder.
    */
    public CacheStats(long hitCount, long missCount, long loadSuccessCount, long loadExceptionCount, long totalLoadTime,
	    long evictionCount) {
	checkArgument(hitCount &gt;= 0);
	checkArgument(missCount &gt;= 0);
	checkArgument(loadSuccessCount &gt;= 0);
	checkArgument(loadExceptionCount &gt;= 0);
	checkArgument(totalLoadTime &gt;= 0);
	checkArgument(evictionCount &gt;= 0);

	this.hitCount = hitCount;
	this.missCount = missCount;
	this.loadSuccessCount = loadSuccessCount;
	this.loadExceptionCount = loadExceptionCount;
	this.totalLoadTime = totalLoadTime;
	this.evictionCount = evictionCount;
    }

}

