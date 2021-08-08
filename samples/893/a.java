import org.apache.commons.math4.geometry.euclidean.threed.Cartesian3D;

class Circle implements Hyperplane&lt;Sphere2D&gt;, Embedding&lt;Sphere2D, Sphere1D&gt; {
    /** Get the reverse of the instance.
     * &lt;p&gt;Get a circle with reversed orientation with respect to the
     * instance. A new object is built, the instance is untouched.&lt;/p&gt;
     * @return a new circle, with orientation opposite to the instance orientation
     */
    public Circle getReverse() {
	return new Circle(pole.negate(), x, y.negate(), tolerance);
    }

    /** Pole or circle center. */
    private Cartesian3D pole;
    /** First axis in the equator plane, origin of the phase angles. */
    private Cartesian3D x;
    /** Second axis in the equator plane, in quadrature with respect to x. */
    private Cartesian3D y;
    /** Tolerance below which close sub-arcs are merged together. */
    private final double tolerance;

    /** Build a circle from its internal components.
     * &lt;p&gt;The circle is oriented in the trigonometric direction around center.&lt;/p&gt;
     * @param pole circle pole
     * @param x first axis in the equator plane
     * @param y second axis in the equator plane
     * @param tolerance tolerance below which close sub-arcs are merged together
     */
    private Circle(final Cartesian3D pole, final Cartesian3D x, final Cartesian3D y, final double tolerance) {
	this.pole = pole;
	this.x = x;
	this.y = y;
	this.tolerance = tolerance;
    }

}

