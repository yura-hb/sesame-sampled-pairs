class CompositeMap&lt;K, V&gt; extends AbstractIterableMap&lt;K, V&gt; implements Serializable {
    /**
     * Associates the specified value with the specified key in this map
     * (optional operation).  If the map previously contained a mapping for
     * this key, the old value is replaced by the specified value.  (A map
     * {@code m} is said to contain a mapping for a key {@code k} if and only
     * if {@link #containsKey(Object) m.containsKey(k)} would return
     * {@code true}.))
     *
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return previous value associated with specified key, or {@code null}
     *         if there was no mapping for key.  A {@code null} return can
     *         also indicate that the map previously associated {@code null}
     *         with the specified key, if the implementation supports
     *         {@code null} values.
     *
     * @throws UnsupportedOperationException if no MapMutator has been specified
     * @throws ClassCastException if the class of the specified key or value
     *            prevents it from being stored in this map.
     * @throws IllegalArgumentException if some aspect of this key or value
     *            prevents it from being stored in this map.
     * @throws NullPointerException this map does not permit {@code null}
     *            keys or values, and the specified key or value is
     *            {@code null}.
     */
    @Override
    public V put(final K key, final V value) {
	if (this.mutator == null) {
	    throw new UnsupportedOperationException("No mutator specified");
	}
	return this.mutator.put(this, this.composite, key, value);
    }

    /** Handle mutation operations */
    private MapMutator&lt;K, V&gt; mutator;
    /** Array of all maps in the composite */
    private Map&lt;K, V&gt;[] composite;

    interface MapMutator&lt;K, V&gt; {
	/** Handle mutation operations */
	private MapMutator&lt;K, V&gt; mutator;
	/** Array of all maps in the composite */
	private Map&lt;K, V&gt;[] composite;

	/**
	 * Called when the CompositeMap.put() method is invoked.
	 *
	 * @param map  the CompositeMap which is being modified
	 * @param composited  array of Maps in the CompositeMap being modified
	 * @param key  key with which the specified value is to be associated.
	 * @param value  value to be associated with the specified key.
	 * @return previous value associated with specified key, or {@code null}
	 *         if there was no mapping for key.  A {@code null} return can
	 *         also indicate that the map previously associated {@code null}
	 *         with the specified key, if the implementation supports
	 *         {@code null} values.
	 *
	 * @throws UnsupportedOperationException if not defined
	 * @throws ClassCastException if the class of the specified key or value
	 *            prevents it from being stored in this map.
	 * @throws IllegalArgumentException if some aspect of this key or value
	 *            prevents it from being stored in this map.
	 * @throws NullPointerException this map does not permit {@code null}
	 *            keys or values, and the specified key or value is
	 *            {@code null}.
	 */
	V put(CompositeMap&lt;K, V&gt; map, Map&lt;K, V&gt;[] composited, K key, V value);

    }

}

