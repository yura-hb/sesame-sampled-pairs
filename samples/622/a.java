class EvaluationBinary extends BaseEvaluation&lt;EvaluationBinary&gt; {
    /**
     * Calculate the G-measure for the given output
     *
     * @param output The specified output
     * @return The G-measure for the specified output
     */
    public double gMeasure(int output) {
	double precision = precision(output);
	double recall = recall(output);
	return EvaluationUtils.gMeasure(precision, recall);
    }

    private int[] countTruePositive;
    private int[] countFalsePositive;
    private int[] countFalseNegative;

    /**
     * Get the precision (tp / (tp + fp)) for the specified output
     */
    public double precision(int outputNum) {
	assertIndex(outputNum);
	//double precision = tp / (double) (tp + fp);
	return countTruePositive[outputNum] / (double) (countTruePositive[outputNum] + countFalsePositive[outputNum]);
    }

    /**
     * Get the recall (tp / (tp + fn)) for the specified output
     */
    public double recall(int outputNum) {
	assertIndex(outputNum);
	return countTruePositive[outputNum] / (double) (countTruePositive[outputNum] + countFalseNegative[outputNum]);
    }

    private void assertIndex(int outputNum) {
	if (countTruePositive == null) {
	    throw new UnsupportedOperationException(
		    "EvaluationBinary does not have any stats: eval must be called first");
	}
	if (outputNum &lt; 0 || outputNum &gt;= countTruePositive.length) {
	    throw new IllegalArgumentException("Invalid input: output number must be between 0 and " + (outputNum - 1)
		    + ". Got index: " + outputNum);
	}
    }

}

