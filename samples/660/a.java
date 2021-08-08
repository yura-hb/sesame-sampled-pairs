abstract class AbstractPatriciaTrie&lt;K, V&gt; extends AbstractBitwiseTrie&lt;K, V&gt; {
    /**
     * Returns the entry associated with the specified key in the
     * PatriciaTrieBase.  Returns null if the map contains no mapping
     * for this key.
     * &lt;p&gt;
     * This may throw ClassCastException if the object is not of type K.
     */
    TrieEntry&lt;K, V&gt; getEntry(final Object k) {
	final K key = castKey(k);
	if (key == null) {
	    return null;
	}

	final int lengthInBits = lengthInBits(key);
	final TrieEntry&lt;K, V&gt; entry = getNearestEntryForKey(key, lengthInBits);
	return !entry.isEmpty() && compareKeys(key, entry.key) ? entry : null;
    }

    /** The root node of the {@link org.apache.commons.collections4.Trie}. */
    private transient TrieEntry&lt;K, V&gt; root = new TrieEntry&lt;&gt;(null, null, -1);

    /**
     * Returns the nearest entry for a given key.  This is useful
     * for finding knowing if a given key exists (and finding the value
     * for it), or for inserting the key.
     *
     * The actual get implementation. This is very similar to
     * selectR but with the exception that it might return the
     * root Entry even if it's empty.
     */
    TrieEntry&lt;K, V&gt; getNearestEntryForKey(final K key, final int lengthInBits) {
	TrieEntry&lt;K, V&gt; current = root.left;
	TrieEntry&lt;K, V&gt; path = root;
	while (true) {
	    if (current.bitIndex &lt;= path.bitIndex) {
		return current;
	    }

	    path = current;
	    if (!isBitSet(key, current.bitIndex, lengthInBits)) {
		current = current.left;
	    } else {
		current = current.right;
	    }
	}
    }

    class TrieEntry&lt;K, V&gt; extends BasicEntry&lt;K, V&gt; {
	/** The root node of the {@link org.apache.commons.collections4.Trie}. */
	private transient TrieEntry&lt;K, V&gt; root = new TrieEntry&lt;&gt;(null, null, -1);

	/**
	 * Whether or not the entry is storing a key.
	 * Only the root can potentially be empty, all other
	 * nodes must have a key.
	 */
	public boolean isEmpty() {
	    return key == null;
	}

    }

}

