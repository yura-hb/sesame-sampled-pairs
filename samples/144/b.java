class Spans {
    class Span implements Comparable&lt;Span&gt; {
	/**
	 * Rank spans according to their starting
	 * position. The end position is ignored
	 * in this ranking.
	 */
	public int compareTo(Span otherSpan) {
	    float otherStart = otherSpan.getStart();
	    int result;

	    if (mStart &lt; otherStart) {
		result = -1;
	    } else if (mStart &gt; otherStart) {
		result = 1;
	    } else {
		result = 0;
	    }

	    return result;
	}

	/**
	 * The span includes the starting point.
	 */
	private float mStart;

	/**
	 * Return the start of the {@code Span}.
	 * The start is considered part of the
	 * half-open interval.
	 */
	final float getStart() {
	    return mStart;
	}

    }

}

