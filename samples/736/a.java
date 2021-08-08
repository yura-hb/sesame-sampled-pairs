import org.apache.commons.math4.RealFieldElement;

class FieldVector3D&lt;T&gt; implements Serializable {
    /** Compute the cross-product of two vectors.
     * @param v1 first vector
     * @param v2 second vector
     * @param &lt;T&gt; the type of the field elements
     * @return the cross product v1 ^ v2 as a new Vector
     */
    public static &lt;T extends RealFieldElement&lt;T&gt;&gt; FieldVector3D&lt;T&gt; crossProduct(final Cartesian3D v1,
	    final FieldVector3D&lt;T&gt; v2) {
	return new FieldVector3D&lt;&gt;(v2.x.linearCombination(v1.getY(), v2.z, -v1.getZ(), v2.y),
		v2.y.linearCombination(v1.getZ(), v2.x, -v1.getX(), v2.z),
		v2.z.linearCombination(v1.getX(), v2.y, -v1.getY(), v2.x));
    }

    /** Abscissa. */
    private final T x;
    /** Height. */
    private final T z;
    /** Ordinate. */
    private final T y;

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

