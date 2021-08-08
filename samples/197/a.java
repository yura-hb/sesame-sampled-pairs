class CacheStats {
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

    private final long hitCount;
    private final long missCount;

}

