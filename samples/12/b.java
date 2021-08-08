class LongAdder extends Striped64 implements Serializable {
    /**
     * Equivalent in effect to {@link #sum} followed by {@link
     * #reset}. This method may apply for example during quiescent
     * points between multithreaded computations.  If there are
     * updates concurrent with this method, the returned value is
     * &lt;em&gt;not&lt;/em&gt; guaranteed to be the final value occurring before
     * the reset.
     *
     * @return the sum
     */
    public long sumThenReset() {
	Cell[] cs = cells;
	long sum = getAndSetBase(0L);
	if (cs != null) {
	    for (Cell c : cs) {
		if (c != null)
		    sum += c.getAndSet(0L);
	    }
	}
	return sum;
    }

}

