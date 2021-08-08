import org.apache.commons.math4.exception.OutOfRangeException;
import org.apache.commons.math4.exception.util.LocalizedFormats;

class MatrixUtils {
    /**
     * Check if a column index is valid.
     *
     * @param m Matrix.
     * @param column Column index to check.
     * @throws OutOfRangeException if {@code column} is not a valid index.
     */
    public static void checkColumnIndex(final AnyMatrix m, final int column) throws OutOfRangeException {
	if (column &lt; 0 || column &gt;= m.getColumnDimension()) {
	    throw new OutOfRangeException(LocalizedFormats.COLUMN_INDEX, column, 0, m.getColumnDimension() - 1);
	}
    }

}

