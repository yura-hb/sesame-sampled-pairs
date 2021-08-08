import java.util.Map;

class CompositeMap&lt;K, V&gt; extends AbstractIterableMap&lt;K, V&gt; implements Serializable {
    /**
     * Returns {@code true} if this map maps one or more keys to the
     * specified value.  More formally, returns {@code true} if and only if
     * this map contains at least one mapping to a value {@code v} such that
     * {@code (value==null ? v==null : value.equals(v))}.  This operation
     * will probably require time linear in the map size for most
     * implementations of the {@code Map} interface.
     *
     * @param value value whose presence in this map is to be tested.
     * @return {@code true} if this map maps one or more keys to the
     *         specified value.
     * @throws ClassCastException if the value is of an inappropriate type for
     *         this map (optional).
     * @throws NullPointerException if the value is {@code null} and this map
     *            does not not permit {@code null} values (optional).
     */
    @Override
    public boolean containsValue(final Object value) {
	for (int i = this.composite.length - 1; i &gt;= 0; --i) {
	    if (this.composite[i].containsValue(value)) {
		return true;
	    }
	}
	return false;
    }

    /** Array of all maps in the composite */
    private Map&lt;K, V&gt;[] composite;

}

