import static com.github.benmanes.caffeine.cache.Caffeine.UNSET_INT;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import com.github.benmanes.caffeine.cache.Caffeine.Strength;

class CaffeineSpec {
    /**
    * Creates a CaffeineSpec from a string.
    *
    * @param specification the string form
    * @return the parsed specification
    */
    @SuppressWarnings("StringSplitter")
    public static @NonNull CaffeineSpec parse(@NonNull String specification) {
	CaffeineSpec spec = new CaffeineSpec(specification);
	for (String option : specification.split(SPLIT_OPTIONS)) {
	    spec.parseOption(option.trim());
	}
	return spec;
    }

    static final String SPLIT_OPTIONS = ",";
    final String specification;
    static final String SPLIT_KEY_VALUE = "=";
    int initialCapacity = UNSET_INT;
    long maximumSize = UNSET_INT;
    long maximumWeight = UNSET_INT;
    @Nullable
    Strength keyStrength;
    @Nullable
    Strength valueStrength;
    long expireAfterAccessDuration = UNSET_INT;
    @Nullable
    TimeUnit expireAfterAccessTimeUnit;
    long expireAfterWriteDuration = UNSET_INT;
    @Nullable
    TimeUnit expireAfterWriteTimeUnit;
    long refreshAfterWriteDuration = UNSET_INT;
    @Nullable
    TimeUnit refreshAfterWriteTimeUnit;
    boolean recordStats;

    private CaffeineSpec(String specification) {
	this.specification = requireNonNull(specification);
    }

    /** Parses and applies the configuration option. */
    void parseOption(String option) {
	if (option.isEmpty()) {
	    return;
	}

	@SuppressWarnings("StringSplitter")
	String[] keyAndValue = option.split(SPLIT_KEY_VALUE);
	requireArgument(keyAndValue.length &lt;= 2, "key-value pair %s with more than one equals sign", option);

	String key = keyAndValue[0].trim();
	String value = (keyAndValue.length == 1) ? null : keyAndValue[1].trim();

	configure(key, value);
    }

    /** Configures the setting. */
    void configure(String key, @Nullable String value) {
	switch (key) {
	case "initialCapacity":
	    initialCapacity(key, value);
	    return;
	case "maximumSize":
	    maximumSize(key, value);
	    return;
	case "maximumWeight":
	    maximumWeight(key, value);
	    return;
	case "weakKeys":
	    weakKeys(value);
	    return;
	case "weakValues":
	    valueStrength(key, value, Strength.WEAK);
	    return;
	case "softValues":
	    valueStrength(key, value, Strength.SOFT);
	    return;
	case "expireAfterAccess":
	    expireAfterAccess(key, value);
	    return;
	case "expireAfterWrite":
	    expireAfterWrite(key, value);
	    return;
	case "refreshAfterWrite":
	    refreshAfterWrite(key, value);
	    return;
	case "recordStats":
	    recordStats(value);
	    return;
	default:
	    throw new IllegalArgumentException("Unknown key " + key);
	}
    }

    /** Configures the initial capacity. */
    void initialCapacity(String key, @Nullable String value) {
	requireArgument(initialCapacity == UNSET_INT, "initial capacity was already set to %,d", initialCapacity);
	initialCapacity = parseInt(key, value);
    }

    /** Configures the maximum size. */
    void maximumSize(String key, @Nullable String value) {
	requireArgument(maximumSize == UNSET_INT, "maximum size was already set to %,d", maximumSize);
	requireArgument(maximumWeight == UNSET_INT, "maximum weight was already set to %,d", maximumWeight);
	maximumSize = parseLong(key, value);
    }

    /** Configures the maximum size. */
    void maximumWeight(String key, @Nullable String value) {
	requireArgument(maximumWeight == UNSET_INT, "maximum weight was already set to %,d", maximumWeight);
	requireArgument(maximumSize == UNSET_INT, "maximum size was already set to %,d", maximumSize);
	maximumWeight = parseLong(key, value);
    }

    /** Configures the keys as weak references. */
    void weakKeys(@Nullable String value) {
	requireArgument(value == null, "weak keys does not take a value");
	requireArgument(keyStrength == null, "weak keys was already set");
	keyStrength = Strength.WEAK;
    }

    /** Configures the value as weak or soft references. */
    void valueStrength(String key, @Nullable String value, Strength strength) {
	requireArgument(value == null, "%s does not take a value", key);
	requireArgument(valueStrength == null, "%s was already set to %s", key, valueStrength);
	valueStrength = strength;
    }

    /** Configures expire after access. */
    void expireAfterAccess(String key, @Nullable String value) {
	requireArgument(expireAfterAccessDuration == UNSET_INT, "expireAfterAccess was already set");
	expireAfterAccessDuration = parseDuration(key, value);
	expireAfterAccessTimeUnit = parseTimeUnit(key, value);
    }

    /** Configures expire after write. */
    void expireAfterWrite(String key, @Nullable String value) {
	requireArgument(expireAfterWriteDuration == UNSET_INT, "expireAfterWrite was already set");
	expireAfterWriteDuration = parseDuration(key, value);
	expireAfterWriteTimeUnit = parseTimeUnit(key, value);
    }

    /** Configures refresh after write. */
    void refreshAfterWrite(String key, @Nullable String value) {
	requireArgument(refreshAfterWriteDuration == UNSET_INT, "refreshAfterWrite was already set");
	refreshAfterWriteDuration = parseDuration(key, value);
	refreshAfterWriteTimeUnit = parseTimeUnit(key, value);
    }

    /** Configures the value as weak or soft references. */
    void recordStats(@Nullable String value) {
	requireArgument(value == null, "record stats does not take a value");
	requireArgument(!recordStats, "record stats was already set");
	recordStats = true;
    }

    /** Returns a parsed int value. */
    static int parseInt(String key, @Nullable String value) {
	requireArgument((value != null) && !value.isEmpty(), "value of key %s was omitted", key);
	try {
	    return Integer.parseInt(value);
	} catch (NumberFormatException e) {
	    throw new IllegalArgumentException(
		    String.format("key %s value was set to %s, must be an integer", key, value), e);
	}
    }

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

    /** Returns a parsed duration value. */
    static long parseDuration(String key, @Nullable String value) {
	requireArgument((value != null) && !value.isEmpty(), "value of key %s omitted", key);
	@SuppressWarnings("NullAway")
	String duration = value.substring(0, value.length() - 1);
	return parseLong(key, duration);
    }

    /** Returns a parsed {@link TimeUnit} value. */
    static TimeUnit parseTimeUnit(String key, @Nullable String value) {
	requireArgument((value != null) && !value.isEmpty(), "value of key %s omitted", key);
	@SuppressWarnings("NullAway")
	char lastChar = Character.toLowerCase(value.charAt(value.length() - 1));
	switch (lastChar) {
	case 'd':
	    return TimeUnit.DAYS;
	case 'h':
	    return TimeUnit.HOURS;
	case 'm':
	    return TimeUnit.MINUTES;
	case 's':
	    return TimeUnit.SECONDS;
	default:
	    throw new IllegalArgumentException(
		    String.format("key %s invalid format; was %s, must end with one of [dDhHmMsS]", key, value));
	}
    }

}

