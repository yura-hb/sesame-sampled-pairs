import org.apache.commons.math4.exception.DimensionMismatchException;

class MatrixUtils {
    /**
     * Check if matrices are multiplication compatible
     *
     * @param left Left hand side matrix.
     * @param right Right hand side matrix.
     * @throws DimensionMismatchException if matrices are not multiplication
     * compatible.
     */
    public static void checkMultiplicationCompatible(final AnyMatrix left, final AnyMatrix right)
	    throws DimensionMismatchException {

	if (left.getColumnDimension() != right.getRowDimension()) {
	    throw new DimensionMismatchException(left.getColumnDimension(), right.getRowDimension());
	}
    }

}

