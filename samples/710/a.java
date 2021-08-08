import org.apache.commons.math4.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math4.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math4.fitting.leastsquares.LevenbergMarquardtOptimizer;

abstract class AbstractCurveFitter {
    /**
     * Fits a curve.
     * This method computes the coefficients of the curve that best
     * fit the sample of observed points.
     *
     * @param points Observations.
     * @return the fitted parameters.
     */
    public double[] fit(Collection&lt;WeightedObservedPoint&gt; points) {
	// Perform the fit.
	return getOptimizer().optimize(getProblem(points)).getPoint().toArray();
    }

    /**
     * Creates an optimizer set up to fit the appropriate curve.
     * &lt;p&gt;
     * The default implementation uses a {@link LevenbergMarquardtOptimizer
     * Levenberg-Marquardt} optimizer.
     * &lt;/p&gt;
     * @return the optimizer to use for fitting the curve to the
     * given {@code points}.
     */
    protected LeastSquaresOptimizer getOptimizer() {
	return new LevenbergMarquardtOptimizer();
    }

    /**
     * Creates a least squares problem corresponding to the appropriate curve.
     *
     * @param points Sample points.
     * @return the least squares problem to use for fitting the curve to the
     * given {@code points}.
     */
    protected abstract LeastSquaresProblem getProblem(Collection&lt;WeightedObservedPoint&gt; points);

}

