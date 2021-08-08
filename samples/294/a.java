class SimpleRegression implements Serializable, UpdatingMultipleLinearRegression {
    /**
     * Clears all data from the model.
     */
    @Override
    public void clear() {
	sumX = 0d;
	sumXX = 0d;
	sumY = 0d;
	sumYY = 0d;
	sumXY = 0d;
	n = 0;
    }

    /** sum of x values */
    private double sumX = 0d;
    /** total variation in x (sum of squared deviations from xbar) */
    private double sumXX = 0d;
    /** sum of y values */
    private double sumY = 0d;
    /** total variation in y (sum of squared deviations from ybar) */
    private double sumYY = 0d;
    /** sum of products */
    private double sumXY = 0d;
    /** number of observations */
    private long n = 0;

}

