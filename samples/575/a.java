class LongAdder extends Striped64 implements Serializable, LongAddable {
    /**
    * Adds the given value.
    *
    * @param x the value to add
    */
    public void add(long x) {
	Cell[] as;
	long b, v;
	int[] hc;
	Cell a;
	int n;
	if ((as = cells) != null || !casBase(b = base, b + x)) {
	    boolean uncontended = true;
	    if ((hc = threadHashCode.get()) == null || as == null || (n = as.length) &lt; 1
		    || (a = as[(n - 1) & hc[0]]) == null || !(uncontended = a.cas(v = a.value, v + x)))
		retryUpdate(x, hc, uncontended);
	}
    }

}

