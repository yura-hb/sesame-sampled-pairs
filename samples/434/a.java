import org.nd4j.linalg.primitives.Pair;
import java.util.*;

class MultiDimensionalMap&lt;K, T, V&gt; implements Serializable {
    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The applyTransformToDestination is backed by the map, so changes to the map are
     * reflected in the applyTransformToDestination, and vice-versa.  If the map is modified
     * while an iteration over the applyTransformToDestination is in progress (except through
     * the iterator's own &lt;tt&gt;remove&lt;/tt&gt; operation, or through the
     * &lt;tt&gt;setValue&lt;/tt&gt; operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The applyTransformToDestination
     * supports element removal, which removes the corresponding
     * mapping from the map, via the &lt;tt&gt;Iterator.remove&lt;/tt&gt;,
     * &lt;tt&gt;Set.remove&lt;/tt&gt;, &lt;tt&gt;removeAll&lt;/tt&gt;, &lt;tt&gt;retainAll&lt;/tt&gt; and
     * &lt;tt&gt;clear&lt;/tt&gt; operations.  It does not support the
     * &lt;tt&gt;add&lt;/tt&gt; or &lt;tt&gt;addAll&lt;/tt&gt; operations.
     *
     * @return a applyTransformToDestination view of the mappings contained in this map
     */

    public Set&lt;Entry&lt;K, T, V&gt;&gt; entrySet() {
	Set&lt;Entry&lt;K, T, V&gt;&gt; ret = new HashSet&lt;&gt;();
	for (Pair&lt;K, T&gt; pair : backedMap.keySet()) {
	    ret.add(new Entry&lt;&gt;(pair.getFirst(), pair.getSecond(), backedMap.get(pair)));
	}
	return ret;
    }

    private Map&lt;Pair&lt;K, T&gt;, V&gt; backedMap;

    class Entry&lt;K, T, V&gt; implements Entry&lt;Pair&lt;K, T&gt;, V&gt; {
	private Map&lt;Pair&lt;K, T&gt;, V&gt; backedMap;

	public Entry(K firstKey, T secondKey, V value) {
	    this.firstKey = firstKey;
	    this.secondKey = secondKey;
	    this.value = value;
	}

    }

}

