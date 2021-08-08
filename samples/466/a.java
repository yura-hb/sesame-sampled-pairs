import org.apache.commons.math4.util.Pair;

class EnumeratedIntegerDistribution extends AbstractIntegerDistribution {
    /**
     * {@inheritDoc}
     *
     * Returns the highest value with non-zero probability.
     *
     * @return the highest value with non-zero probability.
     */
    @Override
    public int getSupportUpperBound() {
	int max = Integer.MIN_VALUE;
	for (final Pair&lt;Integer, Double&gt; sample : innerDistribution.getPmf()) {
	    if (sample.getKey() &gt; max && sample.getValue() &gt; 0) {
		max = sample.getKey();
	    }
	}

	return max;
    }

    /**
     * {@link EnumeratedDistribution} instance (using the {@link Integer} wrapper)
     * used to generate the pmf.
     */
    protected final EnumeratedDistribution&lt;Integer&gt; innerDistribution;

}

