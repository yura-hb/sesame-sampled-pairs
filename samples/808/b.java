import static java.lang.Double.longBitsToDouble;
import java.util.function.DoubleBinaryOperator;

class DoubleAccumulator extends Striped64 implements Serializable {
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
    public double getThenReset() {
	Cell[] cs = cells;
	double result = longBitsToDouble(getAndSetBase(identity));
	if (cs != null) {
	    for (Cell c : cs) {
		if (c != null) {
		    double v = longBitsToDouble(c.getAndSet(identity));
		    result = function.applyAsDouble(result, v);
		}
	    }
	}
	return result;
    }

    private final long identity;
    private final DoubleBinaryOperator function;

}

