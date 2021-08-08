class FastMath {
    /**
     *  Convert degrees to radians, with error of less than 0.5 ULP
     *  @param x angle in degrees
     *  @return x converted into radians
     */
    public static double toRadians(double x) {
	if (Double.isInfinite(x) || x == 0.0) { // Matches +/- 0.0; return correct sign
	    return x;
	}

	// These are PI/180 split into high and low order bits
	final double facta = 0.01745329052209854;
	final double factb = 1.997844754509471E-9;

	double xa = doubleHighPart(x);
	double xb = x - xa;

	double result = xb * factb + xb * facta + xa * factb + xa * facta;
	if (result == 0) {
	    result *= x; // ensure correct sign if calculation underflows
	}
	return result;
    }

    /** Mask used to clear low order 30 bits */
    private static final long MASK_30BITS = -1L - (HEX_40000000 - 1);

    /**
     * Get the high order bits from the mantissa.
     * Equivalent to adding and subtracting HEX_40000 but also works for very large numbers
     *
     * @param d the value to split
     * @return the high order part of the mantissa
     */
    private static double doubleHighPart(double d) {
	if (d &gt; -Precision.SAFE_MIN && d &lt; Precision.SAFE_MIN) {
	    return d; // These are un-normalised - don't try to convert
	}
	long xl = Double.doubleToRawLongBits(d); // can take raw bits because just gonna convert it back
	xl &= MASK_30BITS; // Drop low order bits
	return Double.longBitsToDouble(xl);
    }

}

