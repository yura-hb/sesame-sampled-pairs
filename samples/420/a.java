import java.util.logging.Level;
import java.util.logging.Logger;

class Caffeine&lt;K, V&gt; {
    /**
    * Builds a cache which does not automatically load values when keys are requested unless a
    * mapping function is provided. Note that multiple threads can concurrently load values for
    * distinct keys.
    * &lt;p&gt;
    * Consider {@link #build(CacheLoader)} instead, if it is feasible to implement a
    * {@code CacheLoader}.
    * &lt;p&gt;
    * This method does not alter the state of this {@code Caffeine} instance, so it can be invoked
    * again to create multiple independent caches.
    *
    * @param &lt;K1&gt; the key type of the cache
    * @param &lt;V1&gt; the value type of the cache
    * @return a cache having the requested features
    */
    @NonNull
    public &lt;K1 extends K, V1 extends V&gt; Cache&lt;K1, V1&gt; build() {
	requireWeightWithWeigher();
	requireNonLoadingCache();

	@SuppressWarnings("unchecked")
	Caffeine&lt;K1, V1&gt; self = (Caffeine&lt;K1, V1&gt;) this;
	return isBounded() ? new BoundedLocalCache.BoundedLocalManualCache&lt;&gt;(self)
		: new UnboundedLocalCache.UnboundedLocalManualCache&lt;&gt;(self);
    }

    @Nullable
    Weigher&lt;? super K, ? super V&gt; weigher;
    long maximumWeight = UNSET_INT;
    static final int UNSET_INT = -1;
    boolean strictParsing = true;
    static final Logger logger = Logger.getLogger(Caffeine.class.getName());
    long refreshNanos = UNSET_INT;
    long maximumSize = UNSET_INT;
    long expireAfterAccessNanos = UNSET_INT;
    long expireAfterWriteNanos = UNSET_INT;
    @Nullable
    Expiry&lt;? super K, ? super V&gt; expiry;
    @Nullable
    Strength keyStrength;
    @Nullable
    Strength valueStrength;

    void requireWeightWithWeigher() {
	if (weigher == null) {
	    requireState(maximumWeight == UNSET_INT, "maximumWeight requires weigher");
	} else if (strictParsing) {
	    requireState(maximumWeight != UNSET_INT, "weigher requires maximumWeight");
	} else if (maximumWeight == UNSET_INT) {
	    logger.log(Level.WARNING, "ignoring weigher specified without maximumWeight");
	}
    }

    void requireNonLoadingCache() {
	requireState(refreshNanos == UNSET_INT, "refreshAfterWrite requires a LoadingCache");
    }

    boolean isBounded() {
	return (maximumSize != UNSET_INT) || (maximumWeight != UNSET_INT) || (expireAfterAccessNanos != UNSET_INT)
		|| (expireAfterWriteNanos != UNSET_INT) || (expiry != null) || (keyStrength != null)
		|| (valueStrength != null);
    }

    /** Ensures that the state expression is true. */
    static void requireState(boolean expression, String template, Object... args) {
	if (!expression) {
	    throw new IllegalStateException(String.format(template, args));
	}
    }

}

