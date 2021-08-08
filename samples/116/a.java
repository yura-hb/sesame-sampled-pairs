import org.apache.commons.math4.exception.OutOfRangeException;
import org.apache.commons.math4.exception.util.LocalizedFormats;

abstract class AbstractFieldMatrix&lt;T&gt; implements FieldMatrix&lt;T&gt; {
    /**
     * Check if a row index is valid.
     *
     * @param row Row index to check.
     * @throws OutOfRangeException if {@code index} is not valid.
     */
    protected void checkRowIndex(final int row) throws OutOfRangeException {
	if (row &lt; 0 || row &gt;= getRowDimension()) {
	    throw new OutOfRangeException(LocalizedFormats.ROW_INDEX, row, 0, getRowDimension() - 1);
	}
    }

    /** {@inheritDoc} */
    @Override
    public abstract int getRowDimension();

}

