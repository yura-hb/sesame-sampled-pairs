class FunctionUtils {
    /**
     * Returns the univariate function
     * {@code h(x) = combiner(f(x), g(x)).}
     *
     * @param combiner Combiner function.
     * @param f Function.
     * @param g Function.
     * @return the composite function.
     */
    public static UnivariateFunction combine(final BivariateFunction combiner, final UnivariateFunction f,
	    final UnivariateFunction g) {
	return new UnivariateFunction() {
	    /** {@inheritDoc} */
	    @Override
	    public double value(double x) {
		return combiner.value(f.value(x), g.value(x));
	    }
	};
    }

}

