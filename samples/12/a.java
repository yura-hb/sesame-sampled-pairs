class LongAdder extends Striped64 implements Serializable, LongAddable {
    /**
    * Equivalent in effect to {@link #sum} followed by {@link #reset}. This method may apply for
    * example during quiescent points between multithreaded computations. If there are updates
    * concurrent with this method, the returned value is &lt;em&gt;not&lt;/em&gt; guaranteed to be the final
    * value occurring before the reset.
    *
    * @return the sum
    */
    public long sumThenReset() {
	long sum = base;
	Cell[] as = cells;
	base = 0L;
	if (as != null) {
	    int n = as.length;
	    for (int i = 0; i &lt; n; ++i) {
		Cell a = as[i];
		if (a != null) {
		    sum += a.value;
		    a.value = 0L;
		}
	    }
	}
	return sum;
    }

}

