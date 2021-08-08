class TreeMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements NavigableMap&lt;K, V&gt;, Cloneable, Serializable {
    /**
     * Returns the key corresponding to the specified Entry.
     * @throws NoSuchElementException if the Entry is null
     */
    static &lt;K&gt; K key(Entry&lt;K, ?&gt; e) {
	if (e == null)
	    throw new NoSuchElementException();
	return e.key;
    }

}

