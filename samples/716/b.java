class Range&lt;C&gt; extends RangeGwtSerializationDependencies implements Predicate&lt;C&gt;, Serializable {
    /**
    * Returns a range that contains all values greater than or equal to {@code lower} and less than
    * or equal to {@code upper}.
    *
    * @throws IllegalArgumentException if {@code lower} is greater than {@code upper}
    * @since 14.0
    */
    public static &lt;C extends Comparable&lt;?&gt;&gt; Range&lt;C&gt; closed(C lower, C upper) {
	return create(Cut.belowValue(lower), Cut.aboveValue(upper));
    }

    final Cut&lt;C&gt; lowerBound;
    final Cut&lt;C&gt; upperBound;

    static &lt;C extends Comparable&lt;?&gt;&gt; Range&lt;C&gt; create(Cut&lt;C&gt; lowerBound, Cut&lt;C&gt; upperBound) {
	return new Range&lt;C&gt;(lowerBound, upperBound);
    }

    private Range(Cut&lt;C&gt; lowerBound, Cut&lt;C&gt; upperBound) {
	this.lowerBound = checkNotNull(lowerBound);
	this.upperBound = checkNotNull(upperBound);
	if (lowerBound.compareTo(upperBound) &gt; 0 || lowerBound == Cut.&lt;C&gt;aboveAll()
		|| upperBound == Cut.&lt;C&gt;belowAll()) {
	    throw new IllegalArgumentException("Invalid range: " + toString(lowerBound, upperBound));
	}
    }

    private static String toString(Cut&lt;?&gt; lowerBound, Cut&lt;?&gt; upperBound) {
	StringBuilder sb = new StringBuilder(16);
	lowerBound.describeAsLowerBound(sb);
	sb.append("..");
	upperBound.describeAsUpperBound(sb);
	return sb.toString();
    }

}

