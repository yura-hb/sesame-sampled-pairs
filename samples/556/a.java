import java.util.Map;

class Flat3Map&lt;K, V&gt; implements IterableMap&lt;K, V&gt;, Serializable, Cloneable {
    /**
     * Puts all the values from the specified map into this map.
     *
     * @param map  the map to add
     * @throws NullPointerException if the map is null
     */
    @Override
    public void putAll(final Map&lt;? extends K, ? extends V&gt; map) {
	final int size = map.size();
	if (size == 0) {
	    return;
	}
	if (delegateMap != null) {
	    delegateMap.putAll(map);
	    return;
	}
	if (size &lt; 4) {
	    for (final Map.Entry&lt;? extends K, ? extends V&gt; entry : map.entrySet()) {
		put(entry.getKey(), entry.getValue());
	    }
	} else {
	    convertToMap();
	    delegateMap.putAll(map);
	}
    }

    /** Map, used while in delegate mode */
    private transient AbstractHashedMap&lt;K, V&gt; delegateMap;
    /** The size of the map, used while in flat mode */
    private transient int size;
    /** Key, used while in flat mode */
    private transient K key3;
    /** Value, used while in flat mode */
    private transient V value3;
    /** Key, used while in flat mode */
    private transient K key2;
    /** Value, used while in flat mode */
    private transient V value2;
    /** Key, used while in flat mode */
    private transient K key1;
    /** Value, used while in flat mode */
    private transient V value1;
    /** Hash, used while in flat mode */
    private transient int hash3;
    /** Hash, used while in flat mode */
    private transient int hash2;
    /** Hash, used while in flat mode */
    private transient int hash1;

    /**
     * Puts a key-value mapping into this map.
     *
     * @param key  the key to add
     * @param value  the value to add
     * @return the value previously mapped to this key, null if none
     */
    @Override
    public V put(final K key, final V value) {
	if (delegateMap != null) {
	    return delegateMap.put(key, value);
	}
	// change existing mapping
	if (key == null) {
	    switch (size) { // drop through
	    case 3:
		if (key3 == null) {
		    final V old = value3;
		    value3 = value;
		    return old;
		}
	    case 2:
		if (key2 == null) {
		    final V old = value2;
		    value2 = value;
		    return old;
		}
	    case 1:
		if (key1 == null) {
		    final V old = value1;
		    value1 = value;
		    return old;
		}
	    }
	} else {
	    if (size &gt; 0) {
		final int hashCode = key.hashCode();
		switch (size) { // drop through
		case 3:
		    if (hash3 == hashCode && key.equals(key3)) {
			final V old = value3;
			value3 = value;
			return old;
		    }
		case 2:
		    if (hash2 == hashCode && key.equals(key2)) {
			final V old = value2;
			value2 = value;
			return old;
		    }
		case 1:
		    if (hash1 == hashCode && key.equals(key1)) {
			final V old = value1;
			value1 = value;
			return old;
		    }
		}
	    }
	}

	// add new mapping
	switch (size) {
	default:
	    convertToMap();
	    delegateMap.put(key, value);
	    return null;
	case 2:
	    hash3 = key == null ? 0 : key.hashCode();
	    key3 = key;
	    value3 = value;
	    break;
	case 1:
	    hash2 = key == null ? 0 : key.hashCode();
	    key2 = key;
	    value2 = value;
	    break;
	case 0:
	    hash1 = key == null ? 0 : key.hashCode();
	    key1 = key;
	    value1 = value;
	    break;
	}
	size++;
	return null;
    }

    /**
     * Converts the flat map data to a map.
     */
    private void convertToMap() {
	delegateMap = createDelegateMap();
	switch (size) { // drop through
	case 3:
	    delegateMap.put(key3, value3);
	case 2:
	    delegateMap.put(key2, value2);
	case 1:
	    delegateMap.put(key1, value1);
	case 0:
	    break;
	default:
	    throw new IllegalStateException("Invalid map index: " + size);
	}

	size = 0;
	hash1 = hash2 = hash3 = 0;
	key1 = key2 = key3 = null;
	value1 = value2 = value3 = null;
    }

    /**
     * Create an instance of the map used for storage when in delegation mode.
     * &lt;p&gt;
     * This can be overridden by subclasses to provide a different map implementation.
     * Not every AbstractHashedMap is suitable, identity and reference based maps
     * would be poor choices.
     *
     * @return a new AbstractHashedMap or subclass
     * @since 3.1
     */
    protected AbstractHashedMap&lt;K, V&gt; createDelegateMap() {
	return new HashedMap&lt;&gt;();
    }

}

