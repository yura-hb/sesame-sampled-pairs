import java.util.logging.Level;
import java.util.logging.Logger;

class CacheBuilder&lt;K, V&gt; {
    /**
    * Builds a cache which does not automatically load values when keys are requested.
    *
    * &lt;p&gt;Consider {@link #build(CacheLoader)} instead, if it is feasible to implement a {@code
    * CacheLoader}.
    *
    * &lt;p&gt;This method does not alter the state of this {@code CacheBuilder} instance, so it can be
    * invoked again to create multiple independent caches.
    *
    * @return a cache having the requested features
    * @since 11.0
    */
    public &lt;K1 extends K, V1 extends V&gt; Cache&lt;K1, V1&gt; build() {
	checkWeightWithWeigher();
	checkNonLoadingCache();
	return new LocalCache.LocalManualCache&lt;&gt;(this);
    }

    @MonotonicNonNull
    Weigher&lt;? super K, ? super V&gt; weigher;
    long maximumWeight = UNSET_INT;
    static final int UNSET_INT = -1;
    boolean strictParsing = true;
    private static final Logger logger = Logger.getLogger(CacheBuilder.class.getName());
    long refreshNanos = UNSET_INT;

    private void checkWeightWithWeigher() {
	if (weigher == null) {
	    checkState(maximumWeight == UNSET_INT, "maximumWeight requires weigher");
	} else {
	    if (strictParsing) {
		checkState(maximumWeight != UNSET_INT, "weigher requires maximumWeight");
	    } else {
		if (maximumWeight == UNSET_INT) {
		    logger.log(Level.WARNING, "ignoring weigher specified without maximumWeight");
		}
	    }
	}
    }

    private void checkNonLoadingCache() {
	checkState(refreshNanos == UNSET_INT, "refreshAfterWrite requires a LoadingCache");
    }

}

