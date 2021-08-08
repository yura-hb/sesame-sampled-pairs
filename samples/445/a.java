abstract class AbstractBitwiseTrie&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements Trie&lt;K, V&gt;, Serializable {
    /**
     * Returns whether or not the given bit on the key is set or false if the key is null.
     *
     * @see KeyAnalyzer#isBitSet(Object, int, int)
     */
    final boolean isBitSet(final K key, final int bitIndex, final int lengthInBits) {
	if (key == null) { // root's might be null!
	    return false;
	}
	return keyAnalyzer.isBitSet(key, bitIndex, lengthInBits);
    }

    /**
     * The {@link KeyAnalyzer} that's being used to build the PATRICIA {@link Trie}.
     */
    private final KeyAnalyzer&lt;? super K&gt; keyAnalyzer;

}

