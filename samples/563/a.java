class FastMath {
    /**
     * Returns the hypotenuse of a triangle with sides {@code x} and {@code y}
     * - sqrt(&lt;i&gt;x&lt;/i&gt;&lt;sup&gt;2&lt;/sup&gt;&nbsp;+&lt;i&gt;y&lt;/i&gt;&lt;sup&gt;2&lt;/sup&gt;)&lt;br&gt;
     * avoiding intermediate overflow or underflow.
     *
     * &lt;ul&gt;
     * &lt;li&gt; If either argument is infinite, then the result is positive infinity.&lt;/li&gt;
     * &lt;li&gt; else, if either argument is NaN then the result is NaN.&lt;/li&gt;
     * &lt;/ul&gt;
     *
     * @param x a value
     * @param y a value
     * @return sqrt(&lt;i&gt;x&lt;/i&gt;&lt;sup&gt;2&lt;/sup&gt;&nbsp;+&lt;i&gt;y&lt;/i&gt;&lt;sup&gt;2&lt;/sup&gt;)
     */
    public static double hypot(final double x, final double y) {
	if (Double.isInfinite(x) || Double.isInfinite(y)) {
	    return Double.POSITIVE_INFINITY;
	} else if (Double.isNaN(x) || Double.isNaN(y)) {
	    return Double.NaN;
	} else {

	    final int expX = getExponent(x);
	    final int expY = getExponent(y);
	    if (expX &gt; expY + 27) {
		// y is neglectible with respect to x
		return abs(x);
	    } else if (expY &gt; expX + 27) {
		// x is neglectible with respect to y
		return abs(y);
	    } else {

		// find an intermediate scale to avoid both overflow and underflow
		final int middleExp = (expX + expY) / 2;

		// scale parameters without losing precision
		final double scaledX = scalb(x, -middleExp);
		final double scaledY = scalb(y, -middleExp);

		// compute scaled hypotenuse
		final double scaledH = sqrt(scaledX * scaledX + scaledY * scaledY);

		// remove scaling
		return scalb(scaledH, middleExp);

	    }

	}
    }

    /** Mask used to clear the non-sign part of a long. */
    private static final long MASK_NON_SIGN_LONG = 0x7fffffffffffffffl;

    /**
     * Return the exponent of a double number, removing the bias.
     * &lt;p&gt;
     * For double numbers of the form 2&lt;sup&gt;x&lt;/sup&gt;, the unbiased
     * exponent is exactly x.
     * &lt;/p&gt;
     * @param d number from which exponent is requested
     * @return exponent for d in IEEE754 representation, without bias
     */
    public static int getExponent(final double d) {
	// NaN and Infinite will return 1024 anywho so can use raw bits
	return (int) ((Double.doubleToRawLongBits(d) &gt;&gt;&gt; 52) & 0x7ff) - 1023;
    }

    /**
     * Absolute value.
     * @param x number from which absolute value is requested
     * @return abs(x)
     */
    public static double abs(double x) {
	return Double.longBitsToDouble(MASK_NON_SIGN_LONG & Double.doubleToRawLongBits(x));
    }

    /**
     * Multiply a double number by a power of 2.
     * @param d number to multiply
     * @param n power of 2
     * @return d &times; 2&lt;sup&gt;n&lt;/sup&gt;
     */
    public static double scalb(final double d, final int n) {

	// first simple and fast handling when 2^n can be represented using normal numbers
	if ((n &gt; -1023) && (n &lt; 1024)) {
	    return d * Double.longBitsToDouble(((long) (n + 1023)) &lt;&lt; 52);
	}

	// handle special cases
	if (Double.isNaN(d) || Double.isInfinite(d) || (d == 0)) {
	    return d;
	}
	if (n &lt; -2098) {
	    return (d &gt; 0) ? 0.0 : -0.0;
	}
	if (n &gt; 2097) {
	    return (d &gt; 0) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
	}

	// decompose d
	final long bits = Double.doubleToRawLongBits(d);
	final long sign = bits & 0x8000000000000000L;
	int exponent = ((int) (bits &gt;&gt;&gt; 52)) & 0x7ff;
	long mantissa = bits & 0x000fffffffffffffL;

	// compute scaled exponent
	int scaledExponent = exponent + n;

	if (n &lt; 0) {
	    // we are really in the case n &lt;= -1023
	    if (scaledExponent &gt; 0) {
		// both the input and the result are normal numbers, we only adjust the exponent
		return Double.longBitsToDouble(sign | (((long) scaledExponent) &lt;&lt; 52) | mantissa);
	    } else if (scaledExponent &gt; -53) {
		// the input is a normal number and the result is a subnormal number

		// recover the hidden mantissa bit
		mantissa |= 1L &lt;&lt; 52;

		// scales down complete mantissa, hence losing least significant bits
		final long mostSignificantLostBit = mantissa & (1L &lt;&lt; (-scaledExponent));
		mantissa &gt;&gt;&gt;= 1 - scaledExponent;
		if (mostSignificantLostBit != 0) {
		    // we need to add 1 bit to round up the result
		    mantissa++;
		}
		return Double.longBitsToDouble(sign | mantissa);

	    } else {
		// no need to compute the mantissa, the number scales down to 0
		return (sign == 0L) ? 0.0 : -0.0;
	    }
	} else {
	    // we are really in the case n &gt;= 1024
	    if (exponent == 0) {

		// the input number is subnormal, normalize it
		while ((mantissa &gt;&gt;&gt; 52) != 1) {
		    mantissa &lt;&lt;= 1;
		    --scaledExponent;
		}
		++scaledExponent;
		mantissa &= 0x000fffffffffffffL;

		if (scaledExponent &lt; 2047) {
		    return Double.longBitsToDouble(sign | (((long) scaledExponent) &lt;&lt; 52) | mantissa);
		} else {
		    return (sign == 0L) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
		}

	    } else if (scaledExponent &lt; 2047) {
		return Double.longBitsToDouble(sign | (((long) scaledExponent) &lt;&lt; 52) | mantissa);
	    } else {
		return (sign == 0L) ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
	    }
	}

    }

    /** Compute the square root of a number.
     * &lt;p&gt;&lt;b&gt;Note:&lt;/b&gt; this implementation currently delegates to {@link Math#sqrt}
     * @param a number on which evaluation is done
     * @return square root of a
     */
    public static double sqrt(final double a) {
	return Math.sqrt(a);
    }

}

