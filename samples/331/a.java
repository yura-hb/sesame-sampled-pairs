import java.util.concurrent.ConcurrentHashMap;

class Counter&lt;T&gt; implements Serializable {
    /**
     * This method returns probability of given element
     *
     * @param element
     * @return
     */
    public double getProbability(T element) {
	if (totalCount() &lt;= 0.0)
	    throw new IllegalStateException("Can't calculate probability with empty counter");

	return getCount(element) / totalCount();
    }

    protected AtomicBoolean dirty = new AtomicBoolean(false);
    protected AtomicDouble totalCount = new AtomicDouble(0);
    protected ConcurrentHashMap&lt;T, AtomicDouble&gt; map = new ConcurrentHashMap&lt;&gt;();

    /**
     * This method returns total sum of counter values
     * @return
     */
    public double totalCount() {
	if (dirty.get())
	    rebuildTotals();

	return totalCount.get();
    }

    public double getCount(T element) {
	AtomicDouble t = map.get(element);
	if (t == null)
	    return 0.0;

	return t.get();
    }

    protected void rebuildTotals() {
	totalCount.set(0);
	for (T key : keySet()) {
	    totalCount.addAndGet(getCount(key));
	}

	dirty.set(false);
    }

    /**
     * This method returns Set of elements used in this counter
     *
     * @return
     */
    public Set&lt;T&gt; keySet() {
	return map.keySet();
    }

}

