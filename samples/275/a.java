import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math4.util.FastMath;

class SparseGradient implements RealFieldElement&lt;SparseGradient&gt;, Serializable {
    /** Base 10 logarithm.
     * @return base 10 logarithm of the instance
     */
    @Override
    public SparseGradient log10() {
	return new SparseGradient(FastMath.log10(value), 1.0 / (FastMath.log(10.0) * value), derivatives);
    }

    /** Value of the calculation. */
    private double value;
    /** Stored derivative, each key representing a different independent variable. */
    private final Map&lt;Integer, Double&gt; derivatives;

    /** Internal constructor.
     * @param value value of the function
     * @param scale scaling factor to apply to all derivatives
     * @param derivatives derivatives map, a deep copy will be performed,
     * so the map given here will remain safe from changes in the new instance,
     * may be null to create an empty derivatives map, i.e. a constant value
     */
    private SparseGradient(final double value, final double scale, final Map&lt;Integer, Double&gt; derivatives) {
	this.value = value;
	this.derivatives = new HashMap&lt;&gt;();
	if (derivatives != null) {
	    for (final Map.Entry&lt;Integer, Double&gt; entry : derivatives.entrySet()) {
		this.derivatives.put(entry.getKey(), scale * entry.getValue());
	    }
	}
    }

}

