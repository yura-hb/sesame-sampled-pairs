abstract class AbstractBitwiseTrie&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements Trie&lt;K, V&gt;, Serializable {
    /**
     * Utility method for calling {@link KeyAnalyzer#bitIndex(Object, int, int, Object, int, int)}.
     */
    final int bitIndex(final K key, final K foundKey) {
	return keyAnalyzer.bitIndex(key, 0, lengthInBits(key), foundKey, 0, lengthInBits(foundKey));
    }

    /**
     * The {@link KeyAnalyzer} that's being used to build the PATRICIA {@link Trie}.
     */
    private final KeyAnalyzer&lt;? super K&gt; keyAnalyzer;

    /**
     * Returns the length of the given key in bits
     *
     * @see KeyAnalyzer#lengthInBits(Object)
     */
    final int lengthInBits(final K key) {
	if (key == null) {
	    return 0;
	}

	return keyAnalyzer.lengthInBits(key);
    }

}

