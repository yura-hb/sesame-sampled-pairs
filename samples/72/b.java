class CacheStats {
    /**
    * Returns the average time spent loading new values. This is defined as {@code totalLoadTime /
    * (loadSuccessCount + loadExceptionCount)}.
    */
    public double averageLoadPenalty() {
	long totalLoadCount = loadSuccessCount + loadExceptionCount;
	return (totalLoadCount == 0) ? 0.0 : (double) totalLoadTime / totalLoadCount;
    }

    private final long loadSuccessCount;
    private final long loadExceptionCount;
    private final long totalLoadTime;

}

