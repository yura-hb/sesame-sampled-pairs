import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.math4.linear.Array2DRowRealMatrix;
import org.apache.commons.math4.linear.RealVector;
import org.apache.commons.math4.optim.PointValuePair;

class SimplexTableau implements Serializable {
    /**
     * Get the current solution.
     * @return current solution
     */
    protected PointValuePair getSolution() {
	int negativeVarColumn = columnLabels.indexOf(NEGATIVE_VAR_COLUMN_LABEL);
	Integer negativeVarBasicRow = negativeVarColumn &gt; 0 ? getBasicRow(negativeVarColumn) : null;
	double mostNegative = negativeVarBasicRow == null ? 0 : getEntry(negativeVarBasicRow, getRhsOffset());

	final Set&lt;Integer&gt; usedBasicRows = new HashSet&lt;&gt;();
	final double[] coefficients = new double[getOriginalNumDecisionVariables()];
	for (int i = 0; i &lt; coefficients.length; i++) {
	    int colIndex = columnLabels.indexOf("x" + i);
	    if (colIndex &lt; 0) {
		coefficients[i] = 0;
		continue;
	    }
	    Integer basicRow = getBasicRow(colIndex);
	    if (basicRow != null && basicRow == 0) {
		// if the basic row is found to be the objective function row
		// set the coefficient to 0 -&gt; this case handles unconstrained
		// variables that are still part of the objective function
		coefficients[i] = 0;
	    } else if (usedBasicRows.contains(basicRow)) {
		// if multiple variables can take a given value
		// then we choose the first and set the rest equal to 0
		coefficients[i] = 0 - (restrictToNonNegative ? 0 : mostNegative);
	    } else {
		usedBasicRows.add(basicRow);
		coefficients[i] = (basicRow == null ? 0 : getEntry(basicRow, getRhsOffset()))
			- (restrictToNonNegative ? 0 : mostNegative);
	    }
	}
	return new PointValuePair(coefficients, f.value(coefficients));
    }

    /** The variables each column represents */
    private final List&lt;String&gt; columnLabels = new ArrayList&lt;&gt;();
    /** Column label for negative vars. */
    private static final String NEGATIVE_VAR_COLUMN_LABEL = "x-";
    /** Whether to restrict the variables to non-negative values. */
    private final boolean restrictToNonNegative;
    /** Linear objective function. */
    private final LinearObjectiveFunction f;
    /** Maps basic variables to row they are basic in. */
    private int[] basicVariables;
    /** Simple tableau. */
    private transient Array2DRowRealMatrix tableau;

    /**
     * Checks whether the given column is basic.
     * @param col index of the column to check
     * @return the row that the variable is basic in.  null if the column is not basic
     */
    protected Integer getBasicRow(final int col) {
	final int row = basicVariables[col];
	return row == -1 ? null : row;
    }

    /**
     * Get the offset of the right hand side.
     * @return offset of the right hand side
     */
    protected final int getRhsOffset() {
	return getWidth() - 1;
    }

    /**
     * Get an entry of the tableau.
     * @param row row index
     * @param column column index
     * @return entry at (row, column)
     */
    protected final double getEntry(final int row, final int column) {
	return tableau.getEntry(row, column);
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
     * Get the width of the tableau.
     * @return width of the tableau
     */
    protected final int getWidth() {
	return tableau.getColumnDimension();
    }

}

