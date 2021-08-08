import com.google.common.base.Preconditions;

class CacheTesting {
    /** Warms the given cache by getting all values in {@code [start, end)}, in order. */
    static void warmUp(LoadingCache&lt;Integer, Integer&gt; map, int start, int end) {
	checkNotNull(map);
	for (int i = start; i &lt; end; i++) {
	    map.getUnchecked(i);
	}
    }

}

