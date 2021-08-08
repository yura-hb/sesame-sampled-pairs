import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.commons.math4.exception.MathUnsupportedOperationException;
import org.apache.commons.math4.util.FastMath;

abstract class RealVector {
    /**
     * Returns the L&lt;sub&gt;2&lt;/sub&gt; norm of the vector.
     * &lt;p&gt;The L&lt;sub&gt;2&lt;/sub&gt; norm is the root of the sum of
     * the squared elements.&lt;/p&gt;
     *
     * @return the norm.
     * @see #getL1Norm()
     * @see #getLInfNorm()
     * @see #getDistance(RealVector)
     */
    public double getNorm() {
	double sum = 0;
	Iterator&lt;Entry&gt; it = iterator();
	while (it.hasNext()) {
	    final Entry e = it.next();
	    final double value = e.getValue();
	    sum += value * value;
	}
	return FastMath.sqrt(sum);
    }

    /**
     * Generic dense iterator. Iteration is in increasing order
     * of the vector index.
     *
     * &lt;p&gt;Note: derived classes are required to return an {@link Iterator} that
     * returns non-null {@link Entry} objects as long as {@link Iterator#hasNext()}
     * returns {@code true}.&lt;/p&gt;
     *
     * @return a dense iterator.
     */
    public Iterator&lt;Entry&gt; iterator() {
	final int dim = getDimension();
	return new Iterator&lt;Entry&gt;() {

	    /** Current index. */
	    private int i = 0;

	    /** Current entry. */
	    private Entry e = new Entry();

	    /** {@inheritDoc} */
	    @Override
	    public boolean hasNext() {
		return i &lt; dim;
	    }

	    /** {@inheritDoc} */
	    @Override
	    public Entry next() {
		if (i &lt; dim) {
		    e.setIndex(i++);
		    return e;
		} else {
		    throw new NoSuchElementException();
		}
	    }

	    /**
	     * {@inheritDoc}
	     *
	     * @throws MathUnsupportedOperationException in all circumstances.
	     */
	    @Override
	    public void remove() throws MathUnsupportedOperationException {
		throw new MathUnsupportedOperationException();
	    }
	};
    }

    /**
     * Returns the size of the vector.
     *
     * @return the size of this vector.
     */
    public abstract int getDimension();

    /**
     * Return the entry at the specified index.
     *
     * @param index Index location of entry to be fetched.
     * @return the vector entry at {@code index}.
     * @throws OutOfRangeException if the index is not valid.
     * @see #setEntry(int, double)
     */
    public abstract double getEntry(int index) throws OutOfRangeException;

    class Entry {
	/**
	 * Get the value of the entry.
	 *
	 * @return the value of the entry.
	 */
	public double getValue() {
	    return getEntry(getIndex());
	}

	/** Simple constructor. */
	public Entry() {
	    setIndex(0);
	}

	/**
	 * Set the index of the entry.
	 *
	 * @param index New index for the entry.
	 */
	public void setIndex(int index) {
	    this.index = index;
	}

	/**
	 * Get the index of the entry.
	 *
	 * @return the index of the entry.
	 */
	public int getIndex() {
	    return index;
	}

    }

}

