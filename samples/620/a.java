class FunctionUtils {
    /**
     * Returns a MultivariateFunction h(x[]) defined by &lt;pre&gt; &lt;code&gt;
     * h(x[]) = combiner(...combiner(combiner(initialValue,f(x[0])),f(x[1]))...),f(x[x.length-1]))
     * &lt;/code&gt;&lt;/pre&gt;
     *
     * @param combiner Combiner function.
     * @param f Function.
     * @param initialValue Initial value.
     * @return a collector function.
     */
    public static MultivariateFunction collector(final BivariateFunction combiner, final UnivariateFunction f,
	    final double initialValue) {
	return new MultivariateFunction() {
	    /** {@inheritDoc} */
	    @Override
	    public double value(double[] point) {
		double result = combiner.value(initialValue, f.value(point[0]));
		for (int i = 1; i &lt; point.length; i++) {
		    result = combiner.value(result, f.value(point[i]));
		}
		return result;
	    }
	};
    }

}

