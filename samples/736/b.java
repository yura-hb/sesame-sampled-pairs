class MathUtils {
    /**
     * This returns the determination coefficient of two vectors given a length
     *
     * @param y1 the first vector
     * @param y2 the second vector
     * @param n  the length of both vectors
     * @return the determination coefficient or r^2
     */
    public static double determinationCoefficient(double[] y1, double[] y2, int n) {
	return Math.pow(correlation(y1, y2), 2);
    }

    /**
     * Returns the correlation coefficient of two double vectors.
     *
     * @param residuals       residuals
     * @param targetAttribute target attribute vector
     * @return the correlation coefficient or r
     */
    public static double correlation(double[] residuals, double targetAttribute[]) {
	double[] predictedValues = new double[residuals.length];
	for (int i = 0; i &lt; predictedValues.length; i++) {
	    predictedValues[i] = targetAttribute[i] - residuals[i];
	}
	double ssErr = ssError(predictedValues, targetAttribute);
	double total = ssTotal(residuals, targetAttribute);
	return 1 - (ssErr / total);
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

