import org.apache.commons.math4.exception.NumberIsTooSmallException;
import org.apache.commons.math4.exception.util.LocalizedFormats;

class StorelessBivariateCovariance {
    /**
     * Return the current covariance estimate.
     *
     * @return the current covariance
     * @throws NumberIsTooSmallException if the number of observations
     * is &lt; 2
     */
    public double getResult() throws NumberIsTooSmallException {
	if (n &lt; 2) {
	    throw new NumberIsTooSmallException(LocalizedFormats.INSUFFICIENT_DIMENSION, n, 2, true);
	}
	if (biasCorrected) {
	    return covarianceNumerator / (n - 1d);
	} else {
	    return covarianceNumerator / n;
	}
    }

    /** number of observations */
    private double n;
    /** flag for bias correction */
    private boolean biasCorrected;
    /** the running covariance estimate */
    private double covarianceNumerator;

}

