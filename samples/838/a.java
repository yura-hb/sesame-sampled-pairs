import org.apache.commons.math4.geometry.Point;

class EnclosingBall&lt;S, P&gt; implements Serializable {
    /** Check if a point is within the ball or at boundary.
     * @param point point to test
     * @return true if the point is within the ball or at boundary
     */
    public boolean contains(final P point) {
	return point.distance(center) &lt;= radius;
    }

    /** Center of the ball. */
    private final P center;
    /** Radius of the ball. */
    private final double radius;

}

