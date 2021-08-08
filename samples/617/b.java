class HashMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements Map&lt;K, V&gt;, Cloneable, Serializable {
    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own {@code remove} operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the {@code Iterator.remove},
     * {@code Collection.remove}, {@code removeAll},
     * {@code retainAll} and {@code clear} operations.  It does not
     * support the {@code add} or {@code addAll} operations.
     *
     * @return a view of the values contained in this map
     */
    public Collection&lt;V&gt; values() {
	Collection&lt;V&gt; vs = values;
	if (vs == null) {
	    vs = new Values();
	    values = vs;
	}
	return vs;
    }

}

