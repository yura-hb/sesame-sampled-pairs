class SymbolHash {
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

    /**
     * Array of randomly selected hash function multipliers or &lt;code&gt;null&lt;/code&gt;
     * if the default String.hashCode() function should be used.
     */
    protected int[] fHashMultipliers;
    protected static final int MULTIPLIERS_MASK = MULTIPLIERS_SIZE - 1;

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

