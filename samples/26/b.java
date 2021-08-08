abstract class BaseDistribution implements Distribution {
    /**
     * For a random variable {@code X} whose values are distributed according
     * to this distribution, this method returns {@code P(x0 &lt; X &lt;= x1)}.
     *
     * @param x0 Lower bound (excluded).
     * @param x1 Upper bound (included).
     * @return the probability that a random variable with this distribution
     * takes a value between {@code x0} and {@code x1}, excluding the lower
     * and including the upper endpoint.
     * @throws org.apache.commons.math3.exception.NumberIsTooLargeException if {@code x0 &gt; x1}.
     *                                                                      &lt;p/&gt;
     *                                                                      The default implementation uses the identity
     *                                                                      {@code P(x0 &lt; X &lt;= x1) = P(X &lt;= x1) - P(X &lt;= x0)}
     * @since 3.1
     */

    public double probability(double x0, double x1) {
	if (x0 &gt; x1) {
	    throw new NumberIsTooLargeException(LocalizedFormats.LOWER_ENDPOINT_ABOVE_UPPER_ENDPOINT, x0, x1, true);
	}
	return cumulativeProbability(x1) - cumulativeProbability(x0);
    }

}

