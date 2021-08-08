abstract class BaseDistribution implements Distribution {
    /**
     * {@inheritDoc}
     * &lt;p/&gt;
     * The default implementation returns
     * &lt;ul&gt;
     * &lt;li&gt;{@link #getSupportLowerBound()} for {@code p = 0},&lt;/li&gt;
     * &lt;li&gt;{@link #getSupportUpperBound()} for {@code p = 1}.&lt;/li&gt;
     * &lt;/ul&gt;
     */
    @Override
    public double inverseCumulativeProbability(final double p) throws OutOfRangeException {
	/*
	 * IMPLEMENTATION NOTES
	 * --------------------
	 * Where applicable, use is made of the one-sided Chebyshev inequality
	 * to bracket the root. This inequality states that
	 * P(X - mu &gt;= k * sig) &lt;= 1 / (1 + k^2),
	 * mu: mean, sig: standard deviation. Equivalently
	 * 1 - P(X &lt; mu + k * sig) &lt;= 1 / (1 + k^2),
	 * F(mu + k * sig) &gt;= k^2 / (1 + k^2).
	 *
	 * For k = sqrt(p / (1 - p)), we find
	 * F(mu + k * sig) &gt;= p,
	 * and (mu + k * sig) is an upper-bound for the root.
	 *
	 * Then, introducing Y = -X, mean(Y) = -mu, sd(Y) = sig, and
	 * P(Y &gt;= -mu + k * sig) &lt;= 1 / (1 + k^2),
	 * P(-X &gt;= -mu + k * sig) &lt;= 1 / (1 + k^2),
	 * P(X &lt;= mu - k * sig) &lt;= 1 / (1 + k^2),
	 * F(mu - k * sig) &lt;= 1 / (1 + k^2).
	 *
	 * For k = sqrt((1 - p) / p), we find
	 * F(mu - k * sig) &lt;= p,
	 * and (mu - k * sig) is a lower-bound for the root.
	 *
	 * In cases where the Chebyshev inequality does not apply, geometric
	 * progressions 1, 2, 4, ... and -1, -2, -4, ... are used to bracket
	 * the root.
	 */
	if (p &lt; 0.0 || p &gt; 1.0) {
	    throw new OutOfRangeException(p, 0, 1);
	}

	double lowerBound = getSupportLowerBound();
	if (p == 0.0) {
	    return lowerBound;
	}

	double upperBound = getSupportUpperBound();
	if (p == 1.0) {
	    return upperBound;
	}

	final double mu = getNumericalMean();
	final double sig = FastMath.sqrt(getNumericalVariance());
	final boolean chebyshevApplies;
	chebyshevApplies = !(Double.isInfinite(mu) || Double.isNaN(mu) || Double.isInfinite(sig) || Double.isNaN(sig));

	if (lowerBound == Double.NEGATIVE_INFINITY) {
	    if (chebyshevApplies) {
		lowerBound = mu - sig * FastMath.sqrt((1. - p) / p);
	    } else {
		lowerBound = -1.0;
		while (cumulativeProbability(lowerBound) &gt;= p) {
		    lowerBound *= 2.0;
		}
	    }
	}

	if (upperBound == Double.POSITIVE_INFINITY) {
	    if (chebyshevApplies) {
		upperBound = mu + sig * FastMath.sqrt(p / (1. - p));
	    } else {
		upperBound = 1.0;
		while (cumulativeProbability(upperBound) &lt; p) {
		    upperBound *= 2.0;
		}
	    }
	}

	final UnivariateFunction toSolve = new UnivariateFunction() {

	    public double value(final double x) {
		return cumulativeProbability(x) - p;
	    }
	};

	double x = UnivariateSolverUtils.solve(toSolve, lowerBound, upperBound, getSolverAbsoluteAccuracy());

	if (!isSupportConnected()) {
	    /* Test for plateau. */
	    final double dx = getSolverAbsoluteAccuracy();
	    if (x - dx &gt;= getSupportLowerBound()) {
		double px = cumulativeProbability(x);
		if (cumulativeProbability(x - dx) == px) {
		    upperBound = x;
		    while (upperBound - lowerBound &gt; dx) {
			final double midPoint = 0.5 * (lowerBound + upperBound);
			if (cumulativeProbability(midPoint) &lt; px) {
			    lowerBound = midPoint;
			} else {
			    upperBound = midPoint;
			}
		    }
		    return upperBound;
		}
	    }
	}
	return x;
    }

    protected double solverAbsoluteAccuracy;

    /**
     * Returns the solver absolute accuracy for inverse cumulative computation.
     * You can override this method in order to use a Brent solver with an
     * absolute accuracy different from the default.
     *
     * @return the maximum absolute error in inverse cumulative probability estimates
     */
    protected double getSolverAbsoluteAccuracy() {
	return solverAbsoluteAccuracy;
    }

}

