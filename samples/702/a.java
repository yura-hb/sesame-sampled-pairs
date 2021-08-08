import org.apache.commons.math4.geometry.euclidean.oned.Cartesian1D;

class Line implements Hyperplane&lt;Euclidean2D&gt;, Embedding&lt;Euclidean2D, Euclidean1D&gt; {
    /** Get one point from the plane.
     * @param abscissa desired abscissa for the point
     * @param offset desired offset for the point
     * @return one point in the plane, with given abscissa and offset
     * relative to the line
     */
    public Cartesian2D getPointAt(final Cartesian1D abscissa, final double offset) {
	final double x = abscissa.getX();
	final double dOffset = offset - originOffset;
	return new Cartesian2D(LinearCombination.value(x, cos, dOffset, sin),
		LinearCombination.value(x, sin, -dOffset, cos));
    }

    /** Offset of the frame origin. */
    private double originOffset;
    /** Cosine of the line angle. */
    private double cos;
    /** Sine of the line angle. */
    private double sin;

}

