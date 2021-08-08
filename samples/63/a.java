import java.util.logging.Level;
import java.util.logging.Logger;

class Caffeine&lt;K, V&gt; {
    /**
    * Builds a cache, which either returns an already-loaded value for a given key or atomically
    * computes or retrieves it using the supplied {@code CacheLoader}. If another thread is currently
    * loading the value for this key, simply waits for that thread to finish and returns its loaded
    * value. Note that multiple threads can concurrently load values for distinct keys.
    * &lt;p&gt;
    * This method does not alter the state of this {@code Caffeine} instance, so it can be invoked
    * again to create multiple independent caches.
    *
    * @param loader the cache loader used to obtain new values
    * @param &lt;K1&gt; the key type of the loader
    * @param &lt;V1&gt; the value type of the loader
    * @return a cache having the requested features
    */
    @NonNull
    public &lt;K1 extends K, V1 extends V&gt; LoadingCache&lt;K1, V1&gt; build(@NonNull CacheLoader&lt;? super K1, V1&gt; loader) {
	requireWeightWithWeigher();

	@SuppressWarnings("unchecked")
	Caffeine&lt;K1, V1&gt; self = (Caffeine&lt;K1, V1&gt;) this;
	return isBounded() || refreshes() ? new BoundedLocalCache.BoundedLocalLoadingCache&lt;&gt;(self, loader)
		: new UnboundedLocalCache.UnboundedLocalLoadingCache&lt;&gt;(self, loader);
    }

    @Nullable
    Weigher&lt;? super K, ? super V&gt; weigher;
    long maximumWeight = UNSET_INT;
    static final int UNSET_INT = -1;
    boolean strictParsing = true;
    static final Logger logger = Logger.getLogger(Caffeine.class.getName());
    long maximumSize = UNSET_INT;
    long expireAfterAccessNanos = UNSET_INT;
    long expireAfterWriteNanos = UNSET_INT;
    @Nullable
    Expiry&lt;? super K, ? super V&gt; expiry;
    @Nullable
    Strength keyStrength;
    @Nullable
    Strength valueStrength;
    long refreshNanos = UNSET_INT;

    void requireWeightWithWeigher() {
	if (weigher == null) {
	    requireState(maximumWeight == UNSET_INT, "maximumWeight requires weigher");
	} else if (strictParsing) {
	    requireState(maximumWeight != UNSET_INT, "weigher requires maximumWeight");
	} else if (maximumWeight == UNSET_INT) {
	    logger.log(Level.WARNING, "ignoring weigher specified without maximumWeight");
	}
    }

    boolean isBounded() {
	return (maximumSize != UNSET_INT) || (maximumWeight != UNSET_INT) || (expireAfterAccessNanos != UNSET_INT)
		|| (expireAfterWriteNanos != UNSET_INT) || (expiry != null) || (keyStrength != null)
		|| (valueStrength != null);
    }

    boolean refreshes() {
	return refreshNanos != UNSET_INT;
    }

    /** Ensures that the state expression is true. */
    static void requireState(boolean expression, String template, Object... args) {
	if (!expression) {
	    throw new IllegalStateException(String.format(template, args));
	}
    }

}

