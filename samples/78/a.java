import java.util.*;

class MultiDimensionalMap&lt;K, T, V&gt; implements Serializable {
    /**
     * Tree map implementation
     * @param &lt;K&gt;
     * @param &lt;T&gt;
     * @param &lt;V&gt;
     * @return
     */
    public static &lt;K, T, V&gt; MultiDimensionalMap&lt;K, T, V&gt; newTreeBackedMap() {
	return new MultiDimensionalMap&lt;&gt;(new TreeMap&lt;Pair&lt;K, T&gt;, V&gt;());
    }

    private Map&lt;Pair&lt;K, T&gt;, V&gt; backedMap;

    public MultiDimensionalMap(Map&lt;Pair&lt;K, T&gt;, V&gt; backedMap) {
	this.backedMap = backedMap;
    }

}

