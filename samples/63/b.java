import java.util.logging.Level;
import java.util.logging.Logger;

class CacheBuilder&lt;K, V&gt; {
    /**
    * Builds a cache, which either returns an already-loaded value for a given key or atomically
    * computes or retrieves it using the supplied {@code CacheLoader}. If another thread is currently
    * loading the value for this key, simply waits for that thread to finish and returns its loaded
    * value. Note that multiple threads can concurrently load values for distinct keys.
    *
    * &lt;p&gt;This method does not alter the state of this {@code CacheBuilder} instance, so it can be
    * invoked again to create multiple independent caches.
    *
    * @param loader the cache loader used to obtain new values
    * @return a cache having the requested features
    */
    public &lt;K1 extends K, V1 extends V&gt; LoadingCache&lt;K1, V1&gt; build(CacheLoader&lt;? super K1, V1&gt; loader) {
	checkWeightWithWeigher();
	return new LocalCache.LocalLoadingCache&lt;&gt;(this, loader);
    }

    @MonotonicNonNull
    Weigher&lt;? super K, ? super V&gt; weigher;
    long maximumWeight = UNSET_INT;
    static final int UNSET_INT = -1;
    boolean strictParsing = true;
    private static final Logger logger = Logger.getLogger(CacheBuilder.class.getName());

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

}

