class Hashtable&lt;K, V&gt; extends Dictionary&lt;K, V&gt; implements Map&lt;K, V&gt;, Cloneable, Serializable {
    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * &lt;p&gt;More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key.equals(k))},
     * then this method returns {@code v}; otherwise it returns
     * {@code null}.  (There can be at most one such mapping.)
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or
     *         {@code null} if this map contains no mapping for the key
     * @throws NullPointerException if the specified key is null
     * @see     #put(Object, Object)
     */
    @SuppressWarnings("unchecked")
    public synchronized V get(Object key) {
	Entry&lt;?, ?&gt; tab[] = table;
	int hash = key.hashCode();
	int index = (hash & 0x7FFFFFFF) % tab.length;
	for (Entry&lt;?, ?&gt; e = tab[index]; e != null; e = e.next) {
	    if ((e.hash == hash) && e.key.equals(key)) {
		return (V) e.value;
	    }
	}
	return null;
    }

    /**
     * The hash table data.
     */
    private transient Entry&lt;?, ?&gt;[] table;

}

