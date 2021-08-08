import org.apache.commons.math4.util.FastMath;
import org.apache.commons.math4.util.ResizableDoubleArray;

class DescriptiveStatistics implements StatisticalSummary, Serializable {
    /**
     * Returns the standard deviation of the available values.
     * @return The standard deviation, Double.NaN if no values have been added
     * or 0.0 for a single value set.
     */
    @Override
    public double getStandardDeviation() {
	double stdDev = Double.NaN;
	if (getN() &gt; 0) {
	    if (getN() &gt; 1) {
		stdDev = FastMath.sqrt(getVariance());
	    } else {
		stdDev = 0.0;
	    }
	}
	return stdDev;
    }

    /** Stored data values. */
    private ResizableDoubleArray eDA = new ResizableDoubleArray();
    /** Variance statistic implementation - can be reset by setter. */
    private UnivariateStatistic varianceImpl = new Variance();

    /**
     * Returns the number of available values
     * @return The number of available values
     */
    @Override
    public long getN() {
	return eDA.getNumElements();
    }

    /**
     * Returns the (sample) variance of the available values.
     *
     * &lt;p&gt;This method returns the bias-corrected sample variance (using {@code n - 1} in
     * the denominator).  Use {@link #getPopulationVariance()} for the non-bias-corrected
     * population variance.&lt;/p&gt;
     *
     * @return The variance, Double.NaN if no values have been added
     * or 0.0 for a single value set.
     */
    @Override
    public double getVariance() {
	return apply(varianceImpl);
    }

    /**
     * Apply the given statistic to the data associated with this set of statistics.
     * @param stat the statistic to apply
     * @return the computed value of the statistic.
     */
    public double apply(UnivariateStatistic stat) {
	// No try-catch or advertised exception here because arguments are guaranteed valid
	return eDA.compute(stat);
    }

}

