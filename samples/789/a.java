import java.util.concurrent.ConcurrentHashMap;

class AtomicLongMap&lt;K&gt; implements Serializable {
    /**
    * Returns the sum of all values in this map.
    *
    * &lt;p&gt;This method is not atomic: the sum may or may not include other concurrent operations.
    */
    public long sum() {
	return map.values().stream().mapToLong(Long::longValue).sum();
    }

    private final ConcurrentHashMap&lt;K, Long&gt; map;

}

