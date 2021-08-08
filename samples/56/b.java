import java.util.Map;

class SimpleBindings implements Bindings {
    /**
     * Removes the mapping for this key from this map if it is present
     * (optional operation).   More formally, if this map contains a mapping
     * from key {@code k} to value {@code v} such that
     * {@code (key==null ?  k==null : key.equals(k))}, that mapping
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
     * @throws NullPointerException if key is null
     * @throws ClassCastException if key is not String
     * @throws IllegalArgumentException if key is empty String
     */
    public Object remove(Object key) {
	checkKey(key);
	return map.remove(key);
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

