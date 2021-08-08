import org.nd4j.base.Preconditions;

class NDArrayIndex implements INDArrayIndex {
    /**
     * Generates an interval from begin (inclusive) to end (exclusive)
     *
     * @param begin the begin
     * @param stride  the stride at which to increment
     * @param end   the end index
     * @param max the max length for this domain
     * @return the interval
     */
    public static INDArrayIndex interval(long begin, long stride, long end, long max) {
	if (begin &lt; 0) {
	    begin += max;
	}

	if (end &lt; 0) {
	    end += max;
	}

	if (Math.abs(begin - end) &lt; 1)
	    end++;
	if (stride &gt; 1 && Math.abs(begin - end) == 1) {
	    end *= stride;
	}
	return interval(begin, stride, end, false);
    }

    public static INDArrayIndex interval(long begin, long stride, long end, boolean inclusive) {
	Preconditions.checkArgument(begin &lt;= end,
		"Beginning index (%s) in range must be less than or equal to end (%s)", begin, end);
	INDArrayIndex index = new IntervalIndex(inclusive, stride);
	index.init(begin, end);
	return index;
    }

}

