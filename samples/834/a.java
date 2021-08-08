class MathUtils {
    /**
     * Total variance in target attribute
     *
     * @param residuals       error
     * @param targetAttribute data for target attribute
     * @return Total variance in target attribute
     */
    public static double ssTotal(double[] residuals, double[] targetAttribute) {
	return ssReg(residuals, targetAttribute) + ssError(residuals, targetAttribute);
    }

    /**
     * How much of the variance is explained by the regression
     *
     * @param residuals       error
     * @param targetAttribute data for target attribute
     * @return the sum squares of regression
     */
    public static double ssReg(double[] residuals, double[] targetAttribute) {
	double mean = sum(targetAttribute) / targetAttribute.length;
	double ret = 0;
	for (int i = 0; i &lt; residuals.length; i++) {
	    ret += Math.pow(residuals[i] - mean, 2);
	}
	return ret;
    }

    /**
     * How much of the variance is NOT explained by the regression
     *
     * @param predictedValues predicted values
     * @param targetAttribute data for target attribute
     * @return the sum squares of regression
     */
    public static double ssError(double[] predictedValues, double[] targetAttribute) {
	double ret = 0;
	for (int i = 0; i &lt; predictedValues.length; i++) {
	    ret += Math.pow(targetAttribute[i] - predictedValues[i], 2);
	}
	return ret;
    }

    /**
     * This returns the sum of the given array.
     *
     * @param nums the array of numbers to sum
     * @return the sum of the given array
     */
    public static double sum(double[] nums) {

	double ret = 0;
	for (double d : nums)
	    ret += d;

	return ret;
    }

}

