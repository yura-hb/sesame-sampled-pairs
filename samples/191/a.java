import org.apache.commons.math4.RealFieldElement;

class FieldVector3D&lt;T&gt; implements Serializable {
    /** Compute the cross-product of the instance with another vector.
     * @param v other vector
     * @return the cross product this ^ v as a new FieldVector3D
     */
    public FieldVector3D&lt;T&gt; crossProduct(final FieldVector3D&lt;T&gt; v) {
	return new FieldVector3D&lt;&gt;(x.linearCombination(y, v.z, z.negate(), v.y),
		y.linearCombination(z, v.x, x.negate(), v.z), z.linearCombination(x, v.y, y.negate(), v.x));
    }

    /** Abscissa. */
    private final T x;
    /** Ordinate. */
    private final T y;
    /** Height. */
    private final T z;

    /** Simple constructor.
     * Build a vector from its coordinates
     * @param x abscissa
     * @param y ordinate
     * @param z height
     * @see #getX()
     * @see #getY()
     * @see #getZ()
     */
    public FieldVector3D(final T x, final T y, final T z) {
	this.x = x;
	this.y = y;
	this.z = z;
    }

}

