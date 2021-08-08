class Hashtable&lt;K, V&gt; extends Dictionary&lt;K, V&gt; implements Map&lt;K, V&gt;, Cloneable, Serializable {
    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own {@code remove} operation, or through the
     * {@code setValue} operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the {@code Iterator.remove},
     * {@code Set.remove}, {@code removeAll}, {@code retainAll} and
     * {@code clear} operations.  It does not support the
     * {@code add} or {@code addAll} operations.
     *
     * @since 1.2
     */
    public Set&lt;Map.Entry&lt;K, V&gt;&gt; entrySet() {
	if (entrySet == null)
	    entrySet = Collections.synchronizedSet(new EntrySet(), this);
	return entrySet;
    }

    private transient volatile Set&lt;Map.Entry&lt;K, V&gt;&gt; entrySet;

}

