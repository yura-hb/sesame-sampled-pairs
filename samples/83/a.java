class MultiDimensionalMap&lt;K, T, V&gt; implements Serializable {
    class Entry&lt;K, T, V&gt; implements Entry&lt;Pair&lt;K, T&gt;, V&gt; {
	/**
	 * Replaces the value corresponding to this entry with the specified
	 * value (optional operation).  (Writes through to the map.)  The
	 * behavior of this call is undefined if the mapping has already been
	 * removed from the map (by the iterator's &lt;tt&gt;remove&lt;/tt&gt; operation).
	 *
	 * @param value new value to be stored in this entry
	 * @return old value corresponding to the entry
	 * @throws UnsupportedOperationException if the &lt;tt&gt;put&lt;/tt&gt; operation
	 *                                       is not supported by the backing map
	 * @throws ClassCastException            if the class of the specified value
	 *                                       prevents it from being stored in the backing map
	 * @throws NullPointerException          if the backing map does not permit
	 *                                       null values, and the specified value is null
	 * @throws IllegalArgumentException      if some property of this value
	 *                                       prevents it from being stored in the backing map
	 * @throws IllegalStateException         implementations may, but are not
	 *                                       required to, throw this exception if the entry has been
	 *                                       removed from the backing map.
	 */

	public V setValue(V value) {
	    V old = this.value;
	    this.value = value;
	    return old;
	}

	private V value;

    }

}

