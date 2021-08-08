class ChoiceFormat extends NumberFormat {
    /**
     * Gets the pattern.
     *
     * @return the pattern string
     */
    public String toPattern() {
	StringBuilder result = new StringBuilder();
	for (int i = 0; i &lt; choiceLimits.length; ++i) {
	    if (i != 0) {
		result.append('|');
	    }
	    // choose based upon which has less precision
	    // approximate that by choosing the closest one to an integer.
	    // could do better, but it's not worth it.
	    double less = previousDouble(choiceLimits[i]);
	    double tryLessOrEqual = Math.abs(Math.IEEEremainder(choiceLimits[i], 1.0d));
	    double tryLess = Math.abs(Math.IEEEremainder(less, 1.0d));

	    if (tryLessOrEqual &lt; tryLess) {
		result.append(choiceLimits[i]);
		result.append('#');
	    } else {
		if (choiceLimits[i] == Double.POSITIVE_INFINITY) {
		    result.append("\u221E");
		} else if (choiceLimits[i] == Double.NEGATIVE_INFINITY) {
		    result.append("-\u221E");
		} else {
		    result.append(less);
		}
		result.append('&lt;');
	    }
	    // Append choiceFormats[i], using quotes if there are special characters.
	    // Single quotes themselves must be escaped in either case.
	    String text = choiceFormats[i];
	    boolean needQuote = text.indexOf('&lt;') &gt;= 0 || text.indexOf('#') &gt;= 0 || text.indexOf('\u2264') &gt;= 0
		    || text.indexOf('|') &gt;= 0;
	    if (needQuote)
		result.append('\'');
	    if (text.indexOf('\'') &lt; 0)
		result.append(text);
	    else {
		for (int j = 0; j &lt; text.length(); ++j) {
		    char c = text.charAt(j);
		    result.append(c);
		    if (c == '\'')
			result.append(c);
		}
	    }
	    if (needQuote)
		result.append('\'');
	}
	return result.toString();
    }

    /**
     * A list of lower bounds for the choices.  The formatter will return
     * &lt;code&gt;choiceFormats[i]&lt;/code&gt; if the number being formatted is greater than or equal to
     * &lt;code&gt;choiceLimits[i]&lt;/code&gt; and less than &lt;code&gt;choiceLimits[i+1]&lt;/code&gt;.
     * @serial
     */
    private double[] choiceLimits;
    /**
     * A list of choice strings.  The formatter will return
     * &lt;code&gt;choiceFormats[i]&lt;/code&gt; if the number being formatted is greater than or equal to
     * &lt;code&gt;choiceLimits[i]&lt;/code&gt; and less than &lt;code&gt;choiceLimits[i+1]&lt;/code&gt;.
     * @serial
     */
    private String[] choiceFormats;
    static final long SIGN = 0x8000000000000000L;
    static final long POSITIVEINFINITY = 0x7FF0000000000000L;

    /**
     * Finds the greatest double less than {@code d}.
     * If {@code NaN}, returns same value.
     *
     * @param d the reference value
     * @return the greatest double value less than {@code d}
     * @see #nextDouble
     */
    public static final double previousDouble(double d) {
	return nextDouble(d, false);
    }

    /**
     * Finds the least double greater than {@code d} (if {@code positive} is
     * {@code true}), or the greatest double less than {@code d} (if
     * {@code positive} is {@code false}).
     * If {@code NaN}, returns same value.
     *
     * Does not affect floating-point flags,
     * provided these member functions do not:
     *          Double.longBitsToDouble(long)
     *          Double.doubleToLongBits(double)
     *          Double.isNaN(double)
     *
     * @param d        the reference value
     * @param positive {@code true} if the least double is desired;
     *                 {@code false} otherwise
     * @return the least or greater double value
     */
    public static double nextDouble(double d, boolean positive) {

	/* filter out NaN's */
	if (Double.isNaN(d)) {
	    return d;
	}

	/* zero's are also a special case */
	if (d == 0.0) {
	    double smallestPositiveDouble = Double.longBitsToDouble(1L);
	    if (positive) {
		return smallestPositiveDouble;
	    } else {
		return -smallestPositiveDouble;
	    }
	}

	/* if entering here, d is a nonzero value */

	/* hold all bits in a long for later use */
	long bits = Double.doubleToLongBits(d);

	/* strip off the sign bit */
	long magnitude = bits & ~SIGN;

	/* if next double away from zero, increase magnitude */
	if ((bits &gt; 0) == positive) {
	    if (magnitude != POSITIVEINFINITY) {
		magnitude += 1;
	    }
	}
	/* else decrease magnitude */
	else {
	    magnitude -= 1;
	}

	/* restore sign bit and return */
	long signbit = bits & SIGN;
	return Double.longBitsToDouble(magnitude | signbit);
    }

}

