import java.util.Map;

class CompositeMap&lt;K, V&gt; extends AbstractIterableMap&lt;K, V&gt; implements Serializable {
    /**
     * Returns the number of key-value mappings in this map.  If the
     * map contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * @return the number of key-value mappings in this map.
     */
    @Override
    public int size() {
	int size = 0;
	for (int i = this.composite.length - 1; i &gt;= 0; --i) {
	    size += this.composite[i].size();
	}
	return size;
    }

    /** Array of all maps in the composite */
    private Map&lt;K, V&gt;[] composite;

}

