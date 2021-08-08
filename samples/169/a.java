import static com.github.benmanes.caffeine.cache.Caffeine.UNSET_INT;

class CaffeineSpec {
    /** Configures the maximum size. */
    void maximumWeight(String key, @Nullable String value) {
	requireArgument(maximumWeight == UNSET_INT, "maximum weight was already set to %,d", maximumWeight);
	requireArgument(maximumSize == UNSET_INT, "maximum size was already set to %,d", maximumSize);
	maximumWeight = parseLong(key, value);
    }

    long maximumWeight = UNSET_INT;
    long maximumSize = UNSET_INT;

    /** Returns a parsed long value. */
    static long parseLong(String key, @Nullable String value) {
	requireArgument((value != null) && !value.isEmpty(), "value of key %s was omitted", key);
	try {
	    return Long.parseLong(value);
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException(String.format("key %s value was set to %s, must be a long", key, value),
		    e);
	}
    }

}

