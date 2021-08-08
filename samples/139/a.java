import java.util.Arrays;

class SimpleBounds implements OptimizationData {
    /**
     * Factory method that creates instance of this class that represents
     * unbounded ranges.
     *
     * @param dim Number of parameters.
     * @return a new instance suitable for passing to an optimizer that
     * requires bounds specification.
     */
    public static SimpleBounds unbounded(int dim) {
	final double[] lB = new double[dim];
	Arrays.fill(lB, Double.NEGATIVE_INFINITY);
	final double[] uB = new double[dim];
	Arrays.fill(uB, Double.POSITIVE_INFINITY);

	return new SimpleBounds(lB, uB);
    }

    /** Lower bounds. */
    private final double[] lower;
    /** Upper bounds. */
    private final double[] upper;

    /**
     * @param lB Lower bounds.
     * @param uB Upper bounds.
     */
    public SimpleBounds(double[] lB, double[] uB) {
	lower = lB.clone();
	upper = uB.clone();
    }

}

