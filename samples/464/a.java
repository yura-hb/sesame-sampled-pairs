import org.apache.commons.math4.util.FastMath;

class TravellingSalesmanSolver {
    /**
     * @param x x-coordinate.
     * @param y y-coordinate.
     * @return the city whose coordinates are closest to {@code (x, y)}.
     */
    public City getClosestCity(double x, double y) {
	City closest = null;
	double min = Double.POSITIVE_INFINITY;
	for (City c : cities) {
	    final double d = c.distance(x, y);
	    if (d &lt; min) {
		min = d;
		closest = c;
	    }
	}
	return closest;
    }

    /** Set of cities. */
    private final Set&lt;City&gt; cities = new HashSet&lt;&gt;();

}

class City {
    /**
     * Computes the distance between this city and
     * the given point.
     *
     * @param x x-coodinate.
     * @param y y-coodinate.
     * @return the distance between {@code (x, y)} and this
     * city.
     */
    public double distance(double x, double y) {
	final double xDiff = this.x - x;
	final double yDiff = this.y - y;

	return FastMath.sqrt(xDiff * xDiff + yDiff * yDiff);
    }

    /** x-coordinate. */
    final double x;
    /** y-coordinate. */
    final double y;

}

