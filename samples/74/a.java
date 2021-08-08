import org.apache.commons.math4.exception.DimensionMismatchException;

class MathArrays {
    /**
     * Calculates the L&lt;sub&gt;2&lt;/sub&gt; (Euclidean) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L&lt;sub&gt;2&lt;/sub&gt; distance between the two points
     * @throws DimensionMismatchException if the array lengths differ.
     */
    public static double distance(double[] p1, double[] p2) throws DimensionMismatchException {
	checkEqualLength(p1, p2);
	double sum = 0;
	for (int i = 0; i &lt; p1.length; i++) {
	    final double dp = p1[i] - p2[i];
	    sum += dp * dp;
	}
	return FastMath.sqrt(sum);
    }

    /**
     * Check that both arrays have the same length.
     *
     * @param a Array.
     * @param b Array.
     * @throws DimensionMismatchException if the lengths differ.
     * @since 3.6
     */
    public static void checkEqualLength(double[] a, double[] b) {
	checkEqualLength(a, b, true);
    }

    /**
     * Check that both arrays have the same length.
     *
     * @param a Array.
     * @param b Array.
     * @param abort Whether to throw an exception if the check fails.
     * @return {@code true} if the arrays have the same length.
     * @throws DimensionMismatchException if the lengths differ and
     * {@code abort} is {@code true}.
     * @since 3.6
     */
    public static boolean checkEqualLength(double[] a, double[] b, boolean abort) {
	if (a.length == b.length) {
	    return true;
	} else {
	    if (abort) {
		throw new DimensionMismatchException(a.length, b.length);
	    }
	    return false;
	}
    }

}

