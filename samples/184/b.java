class SymbolHash {
    /**
     * Get the value associated with the given key.
     *
     * @param key
     * @return the value associated with the given key.
     */
    public Object get(Object key) {
	int bucket = hash(key) % fTableSize;
	Entry entry = search(key, bucket);
	if (entry != null) {
	    return entry.value;
	}
	return null;
    }

    /** Actual table size **/
    protected int fTableSize;
    /**
     * Array of randomly selected hash function multipliers or &lt;code&gt;null&lt;/code&gt;
     * if the default String.hashCode() function should be used.
     */
    protected int[] fHashMultipliers;
    /** Buckets. */
    protected Entry[] fBuckets;
    protected static final int MULTIPLIERS_MASK = MULTIPLIERS_SIZE - 1;

    /**
     * Returns a hashcode value for the specified key.
     *
     * @param key The key to hash.
     */
    protected int hash(Object key) {
	if (fHashMultipliers == null || !(key instanceof String)) {
	    return key.hashCode() & 0x7FFFFFFF;
	}
	return hash0((String) key);
    }

    protected Entry search(Object key, int bucket) {
	// search for identical key
	for (Entry entry = fBuckets[bucket]; entry != null; entry = entry.next) {
	    if (key.equals(entry.key))
		return entry;
	}
	return null;
    }

    private int hash0(String symbol) {
	int code = 0;
	final int length = symbol.length();
	final int[] multipliers = fHashMultipliers;
	for (int i = 0; i &lt; length; ++i) {
	    code = code * multipliers[i & MULTIPLIERS_MASK] + symbol.charAt(i);
	}
	return code & 0x7FFFFFFF;
    }

}

