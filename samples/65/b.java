import java.util.Map;

class SimpleBindings implements Bindings {
    /**
     * Returns {@code true} if this map contains a mapping for the specified
     * key.  More formally, returns {@code true} if and only if
     * this map contains a mapping for a key {@code k} such that
     * {@code (key==null ? k==null : key.equals(k))}.  (There can be
     * at most one such mapping.)
     *
     * @param key key whose presence in this map is to be tested.
     * @return {@code true} if this map contains a mapping for the specified
     *         key.
     *
     * @throws NullPointerException if key is null
     * @throws ClassCastException if key is not String
     * @throws IllegalArgumentException if key is empty String
     */
    public boolean containsKey(Object key) {
	checkKey(key);
	return map.containsKey(key);
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

