class LongAdder extends Striped64 implements Serializable, LongAddable {
    /**
    * Returns the current sum. The returned value is &lt;em&gt;NOT&lt;/em&gt; an atomic snapshot; invocation in
    * the absence of concurrent updates returns an accurate result, but concurrent updates that occur
    * while the sum is being calculated might not be incorporated.
    *
    * @return the sum
    */
    public long sum() {
	long sum = base;
	Cell[] as = cells;
	if (as != null) {
	    int n = as.length;
	    for (int i = 0; i &lt; n; ++i) {
		Cell a = as[i];
		if (a != null)
		    sum += a.value;
	    }
	}
	return sum;
    }

}

