class HarmonicCurveFitter extends AbstractCurveFitter {
    class ParameterGuesser {
	/**
	 * Gets an estimation of the parameters.
	 *
	 * @return the guessed parameters, in the following order:
	 * &lt;ul&gt;
	 *  &lt;li&gt;Amplitude&lt;/li&gt;
	 *  &lt;li&gt;Angular frequency&lt;/li&gt;
	 *  &lt;li&gt;Phase&lt;/li&gt;
	 * &lt;/ul&gt;
	 */
	public double[] guess() {
	    return new double[] { a, omega, phi };
	}

	/** Amplitude. */
	private final double a;
	/** Angular frequency. */
	private final double omega;
	/** Phase. */
	private final double phi;

    }

}

