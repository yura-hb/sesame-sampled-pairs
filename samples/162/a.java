import java.util.Map;

class CompositeMap&lt;K, V&gt; extends AbstractIterableMap&lt;K, V&gt; implements Serializable {
    /**
     * Returns {@code true} if this map contains no key-value mappings.
     *
     * @return {@code true} if this map contains no key-value mappings.
     */
    @Override
    public boolean isEmpty() {
	for (int i = this.composite.length - 1; i &gt;= 0; --i) {
	    if (!this.composite[i].isEmpty()) {
		return false;
	    }
	}
	return true;
    }

    /** Array of all maps in the composite */
    private Map&lt;K, V&gt;[] composite;

}

