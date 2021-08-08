class CacheStats {
    /**
    * Returns the ratio of cache requests which were misses. This is defined as {@code missCount /
    * requestCount}, or {@code 0.0} when {@code requestCount == 0}. Note that {@code hitRate +
    * missRate =~ 1.0}. Cache misses include all requests which weren't cache hits, including
    * requests which resulted in either successful or failed loading attempts, and requests which
    * waited for other threads to finish loading. It is thus the case that {@code missCount &gt;=
    * loadSuccessCount + loadExceptionCount}. Multiple concurrent misses for the same key will result
    * in a single load operation.
    */
    public double missRate() {
	long requestCount = requestCount();
	return (requestCount == 0) ? 0.0 : (double) missCount / requestCount;
    }

    private final long missCount;
    private final long hitCount;

    /**
    * Returns the number of times {@link Cache} lookup methods have returned either a cached or
    * uncached value. This is defined as {@code hitCount + missCount}.
    */
    public long requestCount() {
	return hitCount + missCount;
    }

}

