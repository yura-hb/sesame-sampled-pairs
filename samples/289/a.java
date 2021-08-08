import java.util.Map;
import org.apache.commons.math4.exception.DimensionMismatchException;
import org.apache.commons.math4.util.Pair;

abstract class BaseRuleFactory&lt;T&gt; {
    /**
     * Stores a rule.
     *
     * @param rule Rule to be stored.
     * @throws DimensionMismatchException if the elements of the pair do not
     * have the same length.
     */
    protected void addRule(Pair&lt;T[], T[]&gt; rule) throws DimensionMismatchException {
	if (rule.getFirst().length != rule.getSecond().length) {
	    throw new DimensionMismatchException(rule.getFirst().length, rule.getSecond().length);
	}

	pointsAndWeights.put(rule.getFirst().length, rule);
    }

    /** List of points and weights, indexed by the order of the rule. */
    private final Map&lt;Integer, Pair&lt;T[], T[]&gt;&gt; pointsAndWeights = new TreeMap&lt;&gt;();

}

