import org.apache.commons.math4.util.FastMath;
import org.apache.commons.math4.util.OpenIntToDoubleHashMap;
import org.apache.commons.math4.util.OpenIntToDoubleHashMap.Iterator;

class OpenMapRealVector extends SparseRealVector implements Serializable {
    /**
     * Optimized method to subtract OpenMapRealVectors.
     *
     * @param v Vector to subtract from {@code this}.
     * @return the difference of {@code this} and {@code v}.
     * @throws DimensionMismatchException if the dimensions do not match.
     */
    public OpenMapRealVector subtract(OpenMapRealVector v) throws DimensionMismatchException {
	checkVectorDimensions(v.getDimension());
	OpenMapRealVector res = copy();
	Iterator iter = v.getEntries().iterator();
	while (iter.hasNext()) {
	    iter.advance();
	    int key = iter.key();
	    if (entries.containsKey(key)) {
		res.setEntry(key, entries.get(key) - iter.value());
	    } else {
		res.setEntry(key, -iter.value());
	    }
	}
	return res;
    }

    /** Entries of the vector. */
    private final OpenIntToDoubleHashMap entries;
    /** Dimension of the vector. */
    private final int virtualSize;
    /** Tolerance for having a value considered zero. */
    private final double epsilon;

    /** {@inheritDoc} */
    @Override
    public int getDimension() {
	return virtualSize;
    }

    /**
     * {@inheritDoc}
     * @since 2.1
     */
    @Override
    public OpenMapRealVector copy() {
	return new OpenMapRealVector(this);
    }

    /**
     * Get the entries of this instance.
     *
     * @return the entries of this instance.
     */
    private OpenIntToDoubleHashMap getEntries() {
	return entries;
    }

    /** {@inheritDoc} */
    @Override
    public void setEntry(int index, double value) throws OutOfRangeException {
	checkIndex(index);
	if (!isDefaultValue(value)) {
	    entries.put(index, value);
	} else if (entries.containsKey(index)) {
	    entries.remove(index);
	}
    }

    /**
     * Copy constructor.
     *
     * @param v Instance to copy from.
     */
    public OpenMapRealVector(OpenMapRealVector v) {
	virtualSize = v.getDimension();
	entries = new OpenIntToDoubleHashMap(v.getEntries());
	epsilon = v.epsilon;
    }

    /**
     * Determine if this value is within epsilon of zero.
     *
     * @param value Value to test
     * @return {@code true} if this value is within epsilon to zero,
     * {@code false} otherwise.
     * @since 2.1
     */
    protected boolean isDefaultValue(double value) {
	return FastMath.abs(value) &lt; epsilon;
    }

}

