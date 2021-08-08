abstract class AbstractCache&lt;K, V&gt; implements Cache&lt;K, V&gt; {
    class SimpleStatsCounter implements StatsCounter {
	/** Increments all counters by the values in {@code other}. */
	public void incrementBy(StatsCounter other) {
	    CacheStats otherStats = other.snapshot();
	    hitCount.add(otherStats.hitCount());
	    missCount.add(otherStats.missCount());
	    loadSuccessCount.add(otherStats.loadSuccessCount());
	    loadExceptionCount.add(otherStats.loadExceptionCount());
	    totalLoadTime.add(otherStats.totalLoadTime());
	    evictionCount.add(otherStats.evictionCount());
	}

	private final LongAddable hitCount = LongAddables.create();
	private final LongAddable missCount = LongAddables.create();
	private final LongAddable loadSuccessCount = LongAddables.create();
	private final LongAddable loadExceptionCount = LongAddables.create();
	private final LongAddable totalLoadTime = LongAddables.create();
	private final LongAddable evictionCount = LongAddables.create();

    }

    interface StatsCounter {
	/**
	* Returns a snapshot of this counter's values. Note that this may be an inconsistent view, as
	* it may be interleaved with update operations.
	*/
	CacheStats snapshot();

    }

}

