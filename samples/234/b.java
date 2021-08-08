import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;

class CacheBuilderSpec {
    /**
    * Creates a CacheBuilderSpec from a string.
    *
    * @param cacheBuilderSpecification the string form
    */
    public static CacheBuilderSpec parse(String cacheBuilderSpecification) {
	CacheBuilderSpec spec = new CacheBuilderSpec(cacheBuilderSpecification);
	if (!cacheBuilderSpecification.isEmpty()) {
	    for (String keyValuePair : KEYS_SPLITTER.split(cacheBuilderSpecification)) {
		List&lt;String&gt; keyAndValue = ImmutableList.copyOf(KEY_VALUE_SPLITTER.split(keyValuePair));
		checkArgument(!keyAndValue.isEmpty(), "blank key-value pair");
		checkArgument(keyAndValue.size() &lt;= 2, "key-value pair %s with more than one equals sign",
			keyValuePair);

		// Find the ValueParser for the current key.
		String key = keyAndValue.get(0);
		ValueParser valueParser = VALUE_PARSERS.get(key);
		checkArgument(valueParser != null, "unknown key %s", key);

		String value = keyAndValue.size() == 1 ? null : keyAndValue.get(1);
		valueParser.parse(spec, key, value);
	    }
	}

	return spec;
    }

    /** Splits each key-value pair. */
    private static final Splitter KEYS_SPLITTER = Splitter.on(',').trimResults();
    /** Splits the key from the value. */
    private static final Splitter KEY_VALUE_SPLITTER = Splitter.on('=').trimResults();
    /** Map of names to ValueParser. */
    private static final ImmutableMap&lt;String, ValueParser&gt; VALUE_PARSERS = ImmutableMap.&lt;String, ValueParser&gt;builder()
	    .put("initialCapacity", new InitialCapacityParser()).put("maximumSize", new MaximumSizeParser())
	    .put("maximumWeight", new MaximumWeightParser()).put("concurrencyLevel", new ConcurrencyLevelParser())
	    .put("weakKeys", new KeyStrengthParser(Strength.WEAK))
	    .put("softValues", new ValueStrengthParser(Strength.SOFT))
	    .put("weakValues", new ValueStrengthParser(Strength.WEAK)).put("recordStats", new RecordStatsParser())
	    .put("expireAfterAccess", new AccessDurationParser()).put("expireAfterWrite", new WriteDurationParser())
	    .put("refreshAfterWrite", new RefreshDurationParser()).put("refreshInterval", new RefreshDurationParser())
	    .build();
    /** Specification; used for toParseableString(). */
    private final String specification;

    private CacheBuilderSpec(String specification) {
	this.specification = specification;
    }

    interface ValueParser {
	/** Splits each key-value pair. */
	private static final Splitter KEYS_SPLITTER = Splitter.on(',').trimResults();
	/** Splits the key from the value. */
	private static final Splitter KEY_VALUE_SPLITTER = Splitter.on('=').trimResults();
	/** Map of names to ValueParser. */
	private static final ImmutableMap&lt;String, ValueParser&gt; VALUE_PARSERS = ImmutableMap
		.&lt;String, ValueParser&gt;builder().put("initialCapacity", new InitialCapacityParser())
		.put("maximumSize", new MaximumSizeParser()).put("maximumWeight", new MaximumWeightParser())
		.put("concurrencyLevel", new ConcurrencyLevelParser())
		.put("weakKeys", new KeyStrengthParser(Strength.WEAK))
		.put("softValues", new ValueStrengthParser(Strength.SOFT))
		.put("weakValues", new ValueStrengthParser(Strength.WEAK)).put("recordStats", new RecordStatsParser())
		.put("expireAfterAccess", new AccessDurationParser()).put("expireAfterWrite", new WriteDurationParser())
		.put("refreshAfterWrite", new RefreshDurationParser())
		.put("refreshInterval", new RefreshDurationParser()).build();
	/** Specification; used for toParseableString(). */
	private final String specification;

	void parse(CacheBuilderSpec spec, String key, @Nullable String value);

    }

}

