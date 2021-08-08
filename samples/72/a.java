class CacheStats {
    /**
    * Returns the average time spent loading new values. This is defined as
    * {@code totalLoadTime / (loadSuccessCount + loadFailureCount)}.
    *
    * @return the average time spent loading new values
    */
    @NonNegative
    public double averageLoadPenalty() {
	long totalLoadCount = loadSuccessCount + loadFailureCount;
	return (totalLoadCount == 0) ? 0.0 : (double) totalLoadTime / totalLoadCount;
    }

    private final long loadSuccessCount;
    private final long loadFailureCount;
    private final long totalLoadTime;

}

