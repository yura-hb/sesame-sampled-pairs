import org.apache.commons.collections4.OrderedBidiMap;

class UnmodifiableOrderedBidiMap&lt;K, V&gt; extends AbstractOrderedBidiMapDecorator&lt;K, V&gt; implements Unmodifiable {
    /**
     * Gets an unmodifiable view of this map where the keys and values are reversed.
     *
     * @return an inverted unmodifiable bidirectional map
     */
    public OrderedBidiMap&lt;V, K&gt; inverseOrderedBidiMap() {
	if (inverse == null) {
	    inverse = new UnmodifiableOrderedBidiMap&lt;&gt;(decorated().inverseBidiMap());
	    inverse.inverse = this;
	}
	return inverse;
    }

    /** The inverse unmodifiable map */
    private UnmodifiableOrderedBidiMap&lt;V, K&gt; inverse;

    /**
     * Constructor that wraps (not copies).
     *
     * @param map  the map to decorate, must not be null
     * @throws NullPointerException if map is null
     */
    @SuppressWarnings("unchecked") // safe to upcast
    private UnmodifiableOrderedBidiMap(final OrderedBidiMap&lt;? extends K, ? extends V&gt; map) {
	super((OrderedBidiMap&lt;K, V&gt;) map);
    }

}

