import java.util.function.Function;
import com.github.benmanes.caffeine.cache.stats.StatsCounter;

interface LocalCache&lt;K, V&gt; {
    /** Decorates the remapping function to record statistics if enabled. */
    default Function&lt;? super K, ? extends V&gt; statsAware(Function&lt;? super K, ? extends V&gt; mappingFunction,
	    boolean recordLoad) {
	if (!isRecordingStats()) {
	    return mappingFunction;
	}
	return key -&gt; {
	    V value;
	    statsCounter().recordMisses(1);
	    long startTime = statsTicker().read();
	    try {
		value = mappingFunction.apply(key);
	    } catch (RuntimeException | Error e) {
		statsCounter().recordLoadFailure(statsTicker().read() - startTime);
		throw e;
	    }
	    long loadTime = statsTicker().read() - startTime;
	    if (recordLoad) {
		if (value == null) {
		    statsCounter().recordLoadFailure(loadTime);
		} else {
		    statsCounter().recordLoadSuccess(loadTime);
		}
	    }
	    return value;
	};
    }

    /** Returns whether this cache has statistics enabled. */
    boolean isRecordingStats();

    /** Returns the {@link StatsCounter} used by this cache. */
    @NonNull
    StatsCounter statsCounter();

    /** Returns the {@link Ticker} used by this cache for statistics. */
    @NonNull
    Ticker statsTicker();

}

