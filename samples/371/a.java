import static com.google.common.math.DoubleUtils.isFinite;

class DoubleMath {
    /**
    * Returns the &lt;a href="http://en.wikipedia.org/wiki/Arithmetic_mean"&gt;arithmetic mean&lt;/a&gt; of
    * {@code values}.
    *
    * &lt;p&gt;If these values are a sample drawn from a population, this is also an unbiased estimator of
    * the arithmetic mean of the population.
    *
    * @param values a nonempty series of values
    * @throws IllegalArgumentException if {@code values} is empty or contains any non-finite value
    * @deprecated Use {@link Stats#meanOf} instead, noting the less strict handling of non-finite
    *     values.
    */
    @Deprecated
    // com.google.common.math.DoubleUtils
    @GwtIncompatible
    public static double mean(double... values) {
	checkArgument(values.length &gt; 0, "Cannot take mean of 0 values");
	long count = 1;
	double mean = checkFinite(values[0]);
	for (int index = 1; index &lt; values.length; ++index) {
	    checkFinite(values[index]);
	    count++;
	    // Art of Computer Programming vol. 2, Knuth, 4.2.2, (15)
	    mean += (values[index] - mean) / count;
	}
	return mean;
    }

    @GwtIncompatible // com.google.common.math.DoubleUtils
    @CanIgnoreReturnValue
    private static double checkFinite(double argument) {
	checkArgument(isFinite(argument));
	return argument;
    }

}

