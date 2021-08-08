import java.util.Arrays;

class VectorialCovariance implements Serializable {
    /**
     * Clears the internal state of the Statistic
     */
    public void clear() {
	n = 0;
	Arrays.fill(sums, 0.0);
	Arrays.fill(productsSums, 0.0);
    }

    /** Number of vectors in the sample. */
    private long n;
    /** Sums for each component. */
    private final double[] sums;
    /** Sums of products for each component. */
    private final double[] productsSums;

}

