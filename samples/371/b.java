class IntSummaryStatistics implements IntConsumer {
    /**
     * Returns the arithmetic mean of values recorded, or zero if no values have been
     * recorded.
     *
     * @return the arithmetic mean of values, or zero if none
     */
    public final double getAverage() {
	return getCount() &gt; 0 ? (double) getSum() / getCount() : 0.0d;
    }

    private long count;
    private long sum;

    /**
     * Returns the count of values recorded.
     *
     * @return the count of values
     */
    public final long getCount() {
	return count;
    }

    /**
     * Returns the sum of values recorded, or zero if no values have been
     * recorded.
     *
     * @return the sum of values, or zero if none
     */
    public final long getSum() {
	return sum;
    }

}

