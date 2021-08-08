import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math4.util.FastMath;

class SimplexSolver extends LinearOptimizer {
    /**
     * Runs one iteration of the Simplex method on the given model.
     *
     * @param tableau Simple tableau for the problem.
     * @throws TooManyIterationsException if the allowed number of iterations has been exhausted.
     * @throws UnboundedSolutionException if the model is found not to have a bounded solution.
     */
    protected void doIteration(final SimplexTableau tableau)
	    throws TooManyIterationsException, UnboundedSolutionException {

	incrementIterationCount();

	Integer pivotCol = getPivotColumn(tableau);
	Integer pivotRow = getPivotRow(tableau, pivotCol);
	if (pivotRow == null) {
	    throw new UnboundedSolutionException();
	}

	tableau.performRowOperations(pivotCol, pivotRow);
    }

    /** The pivot selection method to use. */
    private PivotSelectionRule pivotSelection;
    /**
     * Cut-off value for entries in the tableau: values smaller than the cut-off
     * are treated as zero to improve numerical stability.
     */
    private final double cutOff;
    /** Amount of error to accept in floating point comparisons (as ulps). */
    private final int maxUlps;

    /**
     * Returns the column with the most negative coefficient in the objective function row.
     *
     * @param tableau Simple tableau for the problem.
     * @return the column with the most negative coefficient.
     */
    private Integer getPivotColumn(SimplexTableau tableau) {
	double minValue = 0;
	Integer minPos = null;
	for (int i = tableau.getNumObjectiveFunctions(); i &lt; tableau.getWidth() - 1; i++) {
	    final double entry = tableau.getEntry(0, i);
	    // check if the entry is strictly smaller than the current minimum
	    // do not use a ulp/epsilon check
	    if (entry &lt; minValue) {
		minValue = entry;
		minPos = i;

		// Bland's rule: chose the entering column with the lowest index
		if (pivotSelection == PivotSelectionRule.BLAND && isValidPivotColumn(tableau, i)) {
		    break;
		}
	    }
	}
	return minPos;
    }

    /**
     * Returns the row with the minimum ratio as given by the minimum ratio test (MRT).
     *
     * @param tableau Simplex tableau for the problem.
     * @param col Column to test the ratio of (see {@link #getPivotColumn(SimplexTableau)}).
     * @return the row with the minimum ratio.
     */
    private Integer getPivotRow(SimplexTableau tableau, final int col) {
	// create a list of all the rows that tie for the lowest score in the minimum ratio test
	List&lt;Integer&gt; minRatioPositions = new ArrayList&lt;&gt;();
	double minRatio = Double.MAX_VALUE;
	for (int i = tableau.getNumObjectiveFunctions(); i &lt; tableau.getHeight(); i++) {
	    final double rhs = tableau.getEntry(i, tableau.getWidth() - 1);
	    final double entry = tableau.getEntry(i, col);

	    // only consider pivot elements larger than the cutOff threshold
	    // selecting others may lead to degeneracy or numerical instabilities
	    if (Precision.compareTo(entry, 0d, cutOff) &gt; 0) {
		final double ratio = FastMath.abs(rhs / entry);
		// check if the entry is strictly equal to the current min ratio
		// do not use a ulp/epsilon check
		final int cmp = Double.compare(ratio, minRatio);
		if (cmp == 0) {
		    minRatioPositions.add(i);
		} else if (cmp &lt; 0) {
		    minRatio = ratio;
		    minRatioPositions.clear();
		    minRatioPositions.add(i);
		}
	    }
	}

	if (minRatioPositions.size() == 0) {
	    return null;
	} else if (minRatioPositions.size() &gt; 1) {
	    // there's a degeneracy as indicated by a tie in the minimum ratio test

	    // 1. check if there's an artificial variable that can be forced out of the basis
	    if (tableau.getNumArtificialVariables() &gt; 0) {
		for (Integer row : minRatioPositions) {
		    for (int i = 0; i &lt; tableau.getNumArtificialVariables(); i++) {
			int column = i + tableau.getArtificialVariableOffset();
			final double entry = tableau.getEntry(row, column);
			if (Precision.equals(entry, 1d, maxUlps) && row.equals(tableau.getBasicRow(column))) {
			    return row;
			}
		    }
		}
	    }

	    // 2. apply Bland's rule to prevent cycling:
	    //    take the row for which the corresponding basic variable has the smallest index
	    //
	    // see http://www.stanford.edu/class/msande310/blandrule.pdf
	    // see http://en.wikipedia.org/wiki/Bland%27s_rule (not equivalent to the above paper)

	    Integer minRow = null;
	    int minIndex = tableau.getWidth();
	    for (Integer row : minRatioPositions) {
		final int basicVar = tableau.getBasicVariable(row);
		if (basicVar &lt; minIndex) {
		    minIndex = basicVar;
		    minRow = row;
		}
	    }
	    return minRow;
	}
	return minRatioPositions.get(0);
    }

    /**
     * Checks whether the given column is valid pivot column, i.e. will result
     * in a valid pivot row.
     * &lt;p&gt;
     * When applying Bland's rule to select the pivot column, it may happen that
     * there is no corresponding pivot row. This method will check if the selected
     * pivot column will return a valid pivot row.
     *
     * @param tableau simplex tableau for the problem
     * @param col the column to test
     * @return {@code true} if the pivot column is valid, {@code false} otherwise
     */
    private boolean isValidPivotColumn(SimplexTableau tableau, int col) {
	for (int i = tableau.getNumObjectiveFunctions(); i &lt; tableau.getHeight(); i++) {
	    final double entry = tableau.getEntry(i, col);

	    // do the same check as in getPivotRow
	    if (Precision.compareTo(entry, 0d, cutOff) &gt; 0) {
		return true;
	    }
	}
	return false;
    }

}

