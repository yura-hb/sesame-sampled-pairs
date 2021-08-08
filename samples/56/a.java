import java.util.Map;

class CompositeMap&lt;K, V&gt; extends AbstractIterableMap&lt;K, V&gt; implements Serializable {
    /**
     * Removes the mapping for this key from this map if it is present
     * (optional operation).   More formally, if this map contains a mapping
     * from key {@code k} to value {@code v} such that
     * &lt;code&gt;(key==null ?  k==null : key.equals(k))&lt;/code&gt;, that mapping
     * is removed.  (The map can contain at most one such mapping.)
     *
     * &lt;p&gt;Returns the value to which the map previously associated the key, or
     * {@code null} if the map contained no mapping for this key.  (A
     * {@code null} return can also indicate that the map previously
     * associated {@code null} with the specified key if the implementation
     * supports {@code null} values.)  The map will not contain a mapping for
     * the specified  key once the call returns.
     *
     * @param key key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or {@code null}
     *         if there was no mapping for key.
     *
     * @throws ClassCastException if the key is of an inappropriate type for
     *         the composited map (optional).
     * @throws NullPointerException if the key is {@code null} and the composited map
     *            does not not permit {@code null} keys (optional).
     * @throws UnsupportedOperationException if the {@code remove} method is
     *         not supported by the composited map containing the key
     */
    @Override
    public V remove(final Object key) {
	for (int i = this.composite.length - 1; i &gt;= 0; --i) {
	    if (this.composite[i].containsKey(key)) {
		return this.composite[i].remove(key);
	    }
	}
	return null;
    }

    /** Array of all maps in the composite */
    private Map&lt;K, V&gt;[] composite;

}

