import java.util.List;
import org.apache.commons.math4.linear.RealVector;

class SimplexTableau implements Serializable {
    /**
     * Initialize the labels for the columns.
     */
    protected void initializeColumnLabels() {
	if (getNumObjectiveFunctions() == 2) {
	    columnLabels.add("W");
	}
	columnLabels.add("Z");
	for (int i = 0; i &lt; getOriginalNumDecisionVariables(); i++) {
	    columnLabels.add("x" + i);
	}
	if (!restrictToNonNegative) {
	    columnLabels.add(NEGATIVE_VAR_COLUMN_LABEL);
	}
	for (int i = 0; i &lt; getNumSlackVariables(); i++) {
	    columnLabels.add("s" + i);
	}
	for (int i = 0; i &lt; getNumArtificialVariables(); i++) {
	    columnLabels.add("a" + i);
	}
	columnLabels.add("RHS");
    }

    /** The variables each column represents */
    private final List&lt;String&gt; columnLabels = new ArrayList&lt;&gt;();
    /** Whether to restrict the variables to non-negative values. */
    private final boolean restrictToNonNegative;
    /** Column label for negative vars. */
    private static final String NEGATIVE_VAR_COLUMN_LABEL = "x-";
    /** Number of artificial variables. */
    private int numArtificialVariables;
    /** Linear objective function. */
    private final LinearObjectiveFunction f;
    /** Number of slack variables. */
    private final int numSlackVariables;

    /**
     * Get the number of objective functions in this tableau.
     * @return 2 for Phase 1.  1 for Phase 2.
     */
    protected final int getNumObjectiveFunctions() {
	return this.numArtificialVariables &gt; 0 ? 2 : 1;
    }

    /**
     * Get the original number of decision variables.
     * @return original number of decision variables
     * @see #getNumDecisionVariables()
     */
    protected final int getOriginalNumDecisionVariables() {
	return f.getCoefficients().getDimension();
    }

    /**
     * Get the number of slack variables.
     * @return number of slack variables
     */
    protected final int getNumSlackVariables() {
	return numSlackVariables;
    }

    /**
     * Get the number of artificial variables.
     * @return number of artificial variables
     */
    protected final int getNumArtificialVariables() {
	return numArtificialVariables;
    }

}

