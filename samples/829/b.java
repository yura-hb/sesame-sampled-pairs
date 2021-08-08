import java.util.function.LongBinaryOperator;

class LongAccumulator extends Striped64 implements Serializable {
    /**
     * Equivalent in effect to {@link #get} followed by {@link
     * #reset}. This method may apply for example during quiescent
     * points between multithreaded computations.  If there are
     * updates concurrent with this method, the returned value is
     * &lt;em&gt;not&lt;/em&gt; guaranteed to be the final value occurring before
     * the reset.
     *
     * @return the value before reset
     */
    public long getThenReset() {
	Cell[] cs = cells;
	long result = getAndSetBase(identity);
	if (cs != null) {
	    for (Cell c : cs) {
		if (c != null) {
		    long v = c.getAndSet(identity);
		    result = function.applyAsLong(result, v);
		}
	    }
	}
	return result;
    }

    private final long identity;
    private final LongBinaryOperator function;

}

