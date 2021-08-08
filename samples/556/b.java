import java.util.*;

class TCustomHashMap&lt;K, V&gt; extends TCustomObjectHash&lt;K&gt; implements TMap&lt;K, V&gt;, Externalizable {
    /**
     * copies the key/value mappings in &lt;tt&gt;map&lt;/tt&gt; into this map.
     *
     * @param map a &lt;code&gt;Map&lt;/code&gt; value
     */
    public void putAll(Map&lt;? extends K, ? extends V&gt; map) {
	ensureCapacity(map.size());
	// could optimize this for cases when map instanceof TCustomHashMap
	for (Map.Entry&lt;? extends K, ? extends V&gt; e : map.entrySet()) {
	    put(e.getKey(), e.getValue());
	}
    }

    /** the values of the  map */
    protected transient V[] _values;

    /**
     * Inserts a key/value pair into the map.
     *
     * @param key   an &lt;code&gt;Object&lt;/code&gt; value
     * @param value an &lt;code&gt;Object&lt;/code&gt; value
     * @return the previous value associated with &lt;tt&gt;key&lt;/tt&gt;,
     *         or {@code null} if none was found.
     */
    public V put(K key, V value) {
	int index = insertKey(key);
	return doPut(value, index);
    }

    private V doPut(V value, int index) {
	V previous = null;
	boolean isNewMapping = true;
	if (index &lt; 0) {
	    index = -index - 1;
	    previous = _values[index];
	    isNewMapping = false;
	}

	_values[index] = value;

	if (isNewMapping) {
	    postInsertHook(consumeFreeSlot);
	}

	return previous;
    }

}

