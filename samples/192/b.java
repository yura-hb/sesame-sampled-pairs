class CacheStats {
    /**
    * Returns the total number of times that {@link Cache} lookup methods attempted to load new
    * values. This includes both successful load operations, as well as those that threw exceptions.
    * This is defined as {@code loadSuccessCount + loadExceptionCount}.
    */
    public long loadCount() {
	return loadSuccessCount + loadExceptionCount;
    }

    private final long loadSuccessCount;
    private final long loadExceptionCount;

}

