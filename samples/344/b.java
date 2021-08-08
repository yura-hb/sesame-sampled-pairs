class LongAdder extends Striped64 implements Serializable {
    /**
     * Returns the current sum.  The returned value is &lt;em&gt;NOT&lt;/em&gt; an
     * atomic snapshot; invocation in the absence of concurrent
     * updates returns an accurate result, but concurrent updates that
     * occur while the sum is being calculated might not be
     * incorporated.
     *
     * @return the sum
     */
    public long sum() {
	Cell[] cs = cells;
	long sum = base;
	if (cs != null) {
	    for (Cell c : cs)
		if (c != null)
		    sum += c.value;
	}
	return sum;
    }

}

