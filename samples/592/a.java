import java.util.Map;
import org.apache.commons.collections4.collection.CompositeCollection;

class CompositeMap&lt;K, V&gt; extends AbstractIterableMap&lt;K, V&gt; implements Serializable {
    /**
     * Returns a collection view of the values contained in this map.  The
     * collection is backed by the map, so changes to the map are reflected in
     * the collection, and vice-versa.  If the map is modified while an
     * iteration over the collection is in progress, the results of the
     * iteration are undefined.  The collection supports element removal,
     * which removes the corresponding mapping from the map, via the
     * {@code Iterator.remove}, {@code Collection.remove},
     * {@code removeAll}, {@code retainAll} and {@code clear} operations.
     * It does not support the add or {@code addAll} operations.
     *
     * @return a collection view of the values contained in this map.
     */
    @Override
    public Collection&lt;V&gt; values() {
	final CompositeCollection&lt;V&gt; values = new CompositeCollection&lt;&gt;();
	for (int i = composite.length - 1; i &gt;= 0; --i) {
	    values.addComposited(composite[i].values());
	}
	return values;
    }

    /** Array of all maps in the composite */
    private Map&lt;K, V&gt;[] composite;

}

