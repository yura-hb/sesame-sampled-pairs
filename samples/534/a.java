import org.apache.commons.math4.analysis.ParametricUnivariateFunction;

abstract class AbstractCurveFitter {
    class TheoreticalValuesFunction {
	/**
	 * @return the model function Jacobian.
	 */
	public MultivariateMatrixFunction getModelFunctionJacobian() {
	    return new MultivariateMatrixFunction() {
		/** {@inheritDoc} */
		@Override
		public double[][] value(double[] p) {
		    final int len = points.length;
		    final double[][] jacobian = new double[len][];
		    for (int i = 0; i &lt; len; i++) {
			jacobian[i] = f.gradient(points[i], p);
		    }
		    return jacobian;
		}
	    };
	}

	/** Observations. */
	private final double[] points;
	/** Function to fit. */
	private final ParametricUnivariateFunction f;

    }

}

