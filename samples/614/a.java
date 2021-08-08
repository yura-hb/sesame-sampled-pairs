import org.apache.commons.math4.util.FastMath;

class Cartesian3D extends Vector3D implements Serializable, Point&lt;Euclidean3D&gt; {
    /** Get the elevation of the vector.
     * @return elevation (&delta;) of the vector, between -&pi;/2 and +&pi;/2
     * @see #Cartesian3D(double, double)
     */
    public double getDelta() {
	return FastMath.asin(z / getNorm());
    }

    /** Height. */
    private final double z;
    /** Abscissa. */
    private final double x;
    /** Ordinate. */
    private final double y;

    /** {@inheritDoc} */
    @Override
    public double getNorm() {
	// there are no cancellation problems here, so we use the straightforward formula
	return FastMath.sqrt(x * x + y * y + z * z);
    }

}

