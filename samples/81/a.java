import java.util.concurrent.atomic.LongAdder;

class ConcurrentStatsCounter implements StatsCounter {
    /**
    * Increments all counters by the values in {@code other}.
    *
    * @param other the counter to increment from
    */
    public void incrementBy(@NonNull StatsCounter other) {
	CacheStats otherStats = other.snapshot();
	hitCount.add(otherStats.hitCount());
	missCount.add(otherStats.missCount());
	loadSuccessCount.add(otherStats.loadSuccessCount());
	loadFailureCount.add(otherStats.loadFailureCount());
	totalLoadTime.add(otherStats.totalLoadTime());
	evictionCount.add(otherStats.evictionCount());
	evictionWeight.add(otherStats.evictionWeight());
    }

    private final LongAdder hitCount;
    private final LongAdder missCount;
    private final LongAdder loadSuccessCount;
    private final LongAdder loadFailureCount;
    private final LongAdder totalLoadTime;
    private final LongAdder evictionCount;
    private final LongAdder evictionWeight;

}

