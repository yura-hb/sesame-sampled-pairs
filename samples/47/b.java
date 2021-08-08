import java.util.Map;

class SimpleBindings implements Bindings {
    /**
     * Returns the value to which this map maps the specified key.  Returns
     * {@code null} if the map contains no mapping for this key.  A return
     * value of {@code null} does not &lt;i&gt;necessarily&lt;/i&gt; indicate that the
     * map contains no mapping for the key; it's also possible that the map
     * explicitly maps the key to {@code null}.  The {@code containsKey}
     * operation may be used to distinguish these two cases.
     *
     * &lt;p&gt;More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that
     * {@code (key==null ? k==null : key.equals(k))},
     * then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     *
     * @param key key whose associated value is to be returned.
     * @return the value to which this map maps the specified key, or
     *         {@code null} if the map contains no mapping for this key.
     *
     * @throws NullPointerException if key is null
     * @throws ClassCastException if key is not String
     * @throws IllegalArgumentException if key is empty String
     */
    public Object get(Object key) {
	checkKey(key);
	return map.get(key);
    }

    /**
     * The {@code Map} field stores the attributes.
     */
    private Map&lt;String, Object&gt; map;

    private void checkKey(Object key) {
	if (key == null) {
	    throw new NullPointerException("key can not be null");
	}
	if (!(key instanceof String)) {
	    throw new ClassCastException("key should be a String");
	}
	if (key.equals("")) {
	    throw new IllegalArgumentException("key can not be empty");
	}
    }

}

