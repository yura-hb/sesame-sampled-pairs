class LongHashMap {
    class Entry {
	/**
	 * Replaces the value corresponding to this entry with the specified
	 * value (optional operation).  (Writes through to the map.)  The
	 * behavior of this call is undefined if the mapping has already been
	 * removed from the map (by the iterator's &lt;tt&gt;remove&lt;/tt&gt; operation).
	 *
	 * @param value new value to be stored in this entry.
	 * @return old value corresponding to the entry.
	 *
	 * @throws UnsupportedOperationException if the &lt;tt&gt;put&lt;/tt&gt; operation
	 *            is not supported by the backing map.
	 * @throws ClassCastException if the class of the specified value
	 *            prevents it from being stored in the backing map.
	 * @throws    IllegalArgumentException if some aspect of this value
	 *            prevents it from being stored in the backing map.
	 * @throws NullPointerException the backing map does not permit
	 *            &lt;tt&gt;null&lt;/tt&gt; values, and the specified value is
	 *            &lt;tt&gt;null&lt;/tt&gt;.
	 */
	Object setValue(Object value) {
	    Object oldValue = this.value;
	    this.value = value;
	    return oldValue;
	}

	private Object value;

    }

}

