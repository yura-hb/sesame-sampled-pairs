class PolynomialSplineFunction implements UnivariateDifferentiableFunction {
    /**
     * Indicates whether a point is within the interpolation range.
     *
     * @param x Point.
     * @return {@code true} if {@code x} is a valid point.
     */
    public boolean isValidPoint(double x) {
	if (x &lt; knots[0] || x &gt; knots[n]) {
	    return false;
	} else {
	    return true;
	}
    }

    /**
     * Spline segment interval delimiters (knots).
     * Size is n + 1 for n segments.
     */
    private final double knots[];
    /**
     * Number of spline segments. It is equal to the number of polynomials and
     * to the number of partition points - 1.
     */
    private final int n;

}

