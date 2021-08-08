class CacheStats {
    /**
    * Returns the ratio of cache requests which were hits. This is defined as
    * {@code hitCount / requestCount}, or {@code 1.0} when {@code requestCount == 0}. Note that
    * {@code hitRate + missRate =~ 1.0}.
    *
    * @return the ratio of cache requests which were hits
    */
    @NonNegative
    public double hitRate() {
	long requestCount = requestCount();
	return (requestCount == 0) ? 1.0 : (double) hitCount / requestCount;
    }

    private final long hitCount;
    private final long missCount;

    /**
    * Returns the number of times {@link Cache} lookup methods have returned either a cached or
    * uncached value. This is defined as {@code hitCount + missCount}.
    *
    * @return the {@code hitCount + missCount}
    */
    @NonNegative
    public long requestCount() {
	return hitCount + missCount;
    }

}

