class DoubleAdder extends Striped64 implements Serializable {
    /**
     * Adds the given value.
     *
     * @param x the value to add
     */
    public void add(double x) {
	Cell[] cs;
	long b, v;
	int m;
	Cell c;
	if ((cs = cells) != null || !casBase(b = base, Double.doubleToRawLongBits(Double.longBitsToDouble(b) + x))) {
	    boolean uncontended = true;
	    if (cs == null || (m = cs.length - 1) &lt; 0 || (c = cs[getProbe() & m]) == null
		    || !(uncontended = c.cas(v = c.value, Double.doubleToRawLongBits(Double.longBitsToDouble(v) + x))))
		doubleAccumulate(x, null, uncontended);
	}
    }

}

