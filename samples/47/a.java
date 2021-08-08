import java.util.Map;

class CompositeMap&lt;K, V&gt; extends AbstractIterableMap&lt;K, V&gt; implements Serializable {
    /**
     * Returns the value to which this map maps the specified key.  Returns
     * {@code null} if the map contains no mapping for this key.  A return
     * value of {@code null} does not &lt;i&gt;necessarily&lt;/i&gt; indicate that the
     * map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to {@code null}.  The {@code containsKey}
     * operation may be used to distinguish these two cases.
     *
     * &lt;p&gt;More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that &lt;code&gt;(key==null ? k==null :
     * key.equals(k))&lt;/code&gt;, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     *
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *         {@code null} if the map contains no mapping for this key.
     *
     * @throws ClassCastException if the key is of an inappropriate type for
     *         this map (optional).
     * @throws NullPointerException key is {@code null} and this map does not
     *         not permit {@code null} keys (optional).
     *
     * @see #containsKey(Object)
     */
    @Override
    public V get(final Object key) {
	for (int i = this.composite.length - 1; i &gt;= 0; --i) {
	    if (this.composite[i].containsKey(key)) {
		return this.composite[i].get(key);
	    }
	}
	return null;
    }

    /** Array of all maps in the composite */
    private Map&lt;K, V&gt;[] composite;

}

