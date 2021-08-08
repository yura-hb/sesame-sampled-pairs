import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class SparseGradient implements RealFieldElement&lt;SparseGradient&gt;, Serializable {
    /** Factory method creating an independent variable.
     * @param idx index of the variable
     * @param value value of the variable
     * @return a new instance
     */
    public static SparseGradient createVariable(final int idx, final double value) {
	return new SparseGradient(value, Collections.singletonMap(idx, 1.0));
    }

    /** Value of the calculation. */
    private double value;
    /** Stored derivative, each key representing a different independent variable. */
    private final Map&lt;Integer, Double&gt; derivatives;

    /** Internal constructor.
     * @param value value of the function
     * @param derivatives derivatives map, a deep copy will be performed,
     * so the map given here will remain safe from changes in the new instance,
     * may be null to create an empty derivatives map, i.e. a constant value
     */
    private SparseGradient(final double value, final Map&lt;Integer, Double&gt; derivatives) {
	this.value = value;
	this.derivatives = new HashMap&lt;&gt;();
	if (derivatives != null) {
	    this.derivatives.putAll(derivatives);
	}
    }

}

