class Line implements Embedding&lt;Euclidean3D, Euclidean1D&gt; {
    /** Compute the point of the instance closest to another line.
     * @param line line to check against the instance
     * @return point of the instance closest to another line
     */
    public Cartesian3D closestPoint(final Line line) {

	final double cos = direction.dotProduct(line.direction);
	final double n = 1 - cos * cos;
	if (n &lt; Precision.EPSILON) {
	    // the lines are parallel
	    return zero;
	}

	final Cartesian3D delta0 = line.zero.subtract(zero);
	final double a = delta0.dotProduct(direction);
	final double b = delta0.dotProduct(line.direction);

	return new Cartesian3D(1, zero, (a - b * cos) / n, direction);

    }

    /** Line direction. */
    private Cartesian3D direction;
    /** Line point closest to the origin. */
    private Cartesian3D zero;

}

