import java.util.Map;

class CompositeMap&lt;K, V&gt; extends AbstractIterableMap&lt;K, V&gt; implements Serializable {
    /**
     * Returns {@code true} if this map contains a mapping for the specified
     * key.  More formally, returns {@code true} if and only if
     * this map contains at a mapping for a key {@code k} such that
     * {@code (key==null ? k==null : key.equals(k))}.  (There can be
     * at most one such mapping.)
     *
     * @param key  key whose presence in this map is to be tested.
     * @return {@code true} if this map contains a mapping for the specified
     *         key.
     *
     * @throws ClassCastException if the key is of an inappropriate type for
     *         this map (optional).
     * @throws NullPointerException if the key is {@code null} and this map
     *            does not not permit {@code null} keys (optional).
     */
    @Override
    public boolean containsKey(final Object key) {
	for (int i = this.composite.length - 1; i &gt;= 0; --i) {
	    if (this.composite[i].containsKey(key)) {
		return true;
	    }
	}
	return false;
    }

    /** Array of all maps in the composite */
    private Map&lt;K, V&gt;[] composite;

}

