class TreeMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements NavigableMap&lt;K, V&gt;, Cloneable, Serializable {
    /**
     * Compares two keys using the correct comparison method for this TreeMap.
     */
    @SuppressWarnings("unchecked")
    final int compare(Object k1, Object k2) {
	return comparator == null ? ((Comparable&lt;? super K&gt;) k1).compareTo((K) k2) : comparator.compare((K) k1, (K) k2);
    }

    /**
     * The comparator used to maintain order in this tree map, or
     * null if it uses the natural ordering of its keys.
     *
     * @serial
     */
    private final Comparator&lt;? super K&gt; comparator;

}

