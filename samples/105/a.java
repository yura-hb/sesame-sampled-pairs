class FastMath {
    /**
     * Power function.  Compute x^y.
     *
     * @param x   a double
     * @param y   a double
     * @return double
     */
    public static double pow(final double x, final double y) {

	if (y == 0) {
	    // y = -0 or y = +0
	    return 1.0;
	} else {

	    final long yBits = Double.doubleToRawLongBits(y);
	    final int yRawExp = (int) ((yBits & MASK_DOUBLE_EXPONENT) &gt;&gt; 52);
	    final long yRawMantissa = yBits & MASK_DOUBLE_MANTISSA;
	    final long xBits = Double.doubleToRawLongBits(x);
	    final int xRawExp = (int) ((xBits & MASK_DOUBLE_EXPONENT) &gt;&gt; 52);
	    final long xRawMantissa = xBits & MASK_DOUBLE_MANTISSA;

	    if (yRawExp &gt; 1085) {
		// y is either a very large integral value that does not fit in a long or it is a special number

		if ((yRawExp == 2047 && yRawMantissa != 0) || (xRawExp == 2047 && xRawMantissa != 0)) {
		    // NaN
		    return Double.NaN;
		} else if (xRawExp == 1023 && xRawMantissa == 0) {
		    // x = -1.0 or x = +1.0
		    if (yRawExp == 2047) {
			// y is infinite
			return Double.NaN;
		    } else {
			// y is a large even integer
			return 1.0;
		    }
		} else {
		    // the absolute value of x is either greater or smaller than 1.0

		    // if yRawExp == 2047 and mantissa is 0, y = -infinity or y = +infinity
		    // if 1085 &lt; yRawExp &lt; 2047, y is simply a large number, however, due to limited
		    // accuracy, at this magnitude it behaves just like infinity with regards to x
		    if ((y &gt; 0) ^ (xRawExp &lt; 1023)) {
			// either y = +infinity (or large engouh) and abs(x) &gt; 1.0
			// or     y = -infinity (or large engouh) and abs(x) &lt; 1.0
			return Double.POSITIVE_INFINITY;
		    } else {
			// either y = +infinity (or large engouh) and abs(x) &lt; 1.0
			// or     y = -infinity (or large engouh) and abs(x) &gt; 1.0
			return +0.0;
		    }
		}

	    } else {
		// y is a regular non-zero number

		if (yRawExp &gt;= 1023) {
		    // y may be an integral value, which should be handled specifically
		    final long yFullMantissa = IMPLICIT_HIGH_BIT | yRawMantissa;
		    if (yRawExp &lt; 1075) {
			// normal number with negative shift that may have a fractional part
			final long integralMask = (-1L) &lt;&lt; (1075 - yRawExp);
			if ((yFullMantissa & integralMask) == yFullMantissa) {
			    // all fractional bits are 0, the number is really integral
			    final long l = yFullMantissa &gt;&gt; (1075 - yRawExp);
			    return FastMath.pow(x, (y &lt; 0) ? -l : l);
			}
		    } else {
			// normal number with positive shift, always an integral value
			// we know it fits in a primitive long because yRawExp &gt; 1085 has been handled above
			final long l = yFullMantissa &lt;&lt; (yRawExp - 1075);
			return FastMath.pow(x, (y &lt; 0) ? -l : l);
		    }
		}

		// y is a non-integral value

		if (x == 0) {
		    // x = -0 or x = +0
		    // the integer powers have already been handled above
		    return y &lt; 0 ? Double.POSITIVE_INFINITY : +0.0;
		} else if (xRawExp == 2047) {
		    if (xRawMantissa == 0) {
			// x = -infinity or x = +infinity
			return (y &lt; 0) ? +0.0 : Double.POSITIVE_INFINITY;
		    } else {
			// NaN
			return Double.NaN;
		    }
		} else if (x &lt; 0) {
		    // the integer powers have already been handled above
		    return Double.NaN;
		} else {

		    // this is the general case, for regular fractional numbers x and y

		    // Split y into ya and yb such that y = ya+yb
		    final double tmp = y * HEX_40000000;
		    final double ya = (y + tmp) - tmp;
		    final double yb = y - ya;

		    /* Compute ln(x) */
		    final double lns[] = new double[2];
		    final double lores = log(x, lns);
		    if (Double.isInfinite(lores)) { // don't allow this to be converted to NaN
			return lores;
		    }

		    double lna = lns[0];
		    double lnb = lns[1];

		    /* resplit lns */
		    final double tmp1 = lna * HEX_40000000;
		    final double tmp2 = (lna + tmp1) - tmp1;
		    lnb += lna - tmp2;
		    lna = tmp2;

		    // y*ln(x) = (aa+ab)
		    final double aa = lna * ya;
		    final double ab = lna * yb + lnb * ya + lnb * yb;

		    lna = aa + ab;
		    lnb = -(lna - aa - ab);

		    double z = 1.0 / 120.0;
		    z = z * lnb + (1.0 / 24.0);
		    z = z * lnb + (1.0 / 6.0);
		    z = z * lnb + 0.5;
		    z = z * lnb + 1.0;
		    z *= lnb;

		    final double result = exp(lna, z, null);
		    //result = result + result * z;
		    return result;

		}
	    }

	}

    }

    /** Mask used to extract exponent from double bits. */
    private static final long MASK_DOUBLE_EXPONENT = 0x7ff0000000000000L;
    /** Mask used to extract mantissa from double bits. */
    private static final long MASK_DOUBLE_MANTISSA = 0x000fffffffffffffL;
    /** Mask used to add implicit high order bit for normalized double. */
    private static final long IMPLICIT_HIGH_BIT = 0x0010000000000000L;
    /**
     * 0x40000000 - used to split a double into two parts, both with the low order bits cleared.
     * Equivalent to 2^30.
     */
    private static final long HEX_40000000 = 0x40000000L;
    /** Coefficients for log, when input 0.99 &lt; x &lt; 1.01. */
    private static final double LN_QUICK_COEF[][] = { { 1.0, 5.669184079525E-24 }, { -0.25, -0.25 },
	    { 0.3333333134651184, 1.986821492305628E-8 }, { -0.25, -6.663542893624021E-14 },
	    { 0.19999998807907104, 1.1921056801463227E-8 }, { -0.1666666567325592, -7.800414592973399E-9 },
	    { 0.1428571343421936, 5.650007086920087E-9 }, { -0.12502530217170715, -7.44321345601866E-11 },
	    { 0.11113807559013367, 9.219544613762692E-9 }, };
    /** 2^52 - double numbers this large must be integral (no fraction) or NaN or Infinite */
    private static final double TWO_POWER_52 = 4503599627370496.0;
    /** Coefficients for log in the range of 1.0 &lt; x &lt; 1.0 + 2^-10. */
    private static final double LN_HI_PREC_COEF[][] = { { 1.0, -6.032174644509064E-23 }, { -0.25, -0.25 },
	    { 0.3333333134651184, 1.9868161777724352E-8 }, { -0.2499999701976776, -2.957007209750105E-8 },
	    { 0.19999954104423523, 1.5830993332061267E-10 }, { -0.16624879837036133, -2.6033824355191673E-8 } };
    /** log(2) (high bits). */
    private static final double LN_2_A = 0.693147063255310059;
    /** log(2) (low bits). */
    private static final double LN_2_B = 1.17304635250823482e-7;
    /** Index of exp(0) in the array of integer exponentials. */
    static final int EXP_INT_TABLE_MAX_INDEX = 750;
    /** Mask used to clear the non-sign part of a long. */
    private static final long MASK_NON_SIGN_LONG = 0x7fffffffffffffffl;

    /**
     * Raise a double to a long power.
     *
     * @param d Number to raise.
     * @param e Exponent.
     * @return d&lt;sup&gt;e&lt;/sup&gt;
     * @since 3.6
     */
    public static double pow(double d, long e) {
	if (e == 0) {
	    return 1.0;
	} else if (e &gt; 0) {
	    return new Split(d).pow(e).full;
	} else {
	    return new Split(d).reciprocal().pow(-e).full;
	}
    }

    /**
     * Internal helper method for natural logarithm function.
     * @param x original argument of the natural logarithm function
     * @param hiPrec extra bits of precision on output (To Be Confirmed)
     * @return log(x)
     */
    private static double log(final double x, final double[] hiPrec) {
	if (x == 0) { // Handle special case of +0/-0
	    return Double.NEGATIVE_INFINITY;
	}
	long bits = Double.doubleToRawLongBits(x);

	/* Handle special cases of negative input, and NaN */
	if (((bits & 0x8000000000000000L) != 0 || Double.isNaN(x)) && x != 0.0) {
	    if (hiPrec != null) {
		hiPrec[0] = Double.NaN;
	    }

	    return Double.NaN;
	}

	/* Handle special cases of Positive infinity. */
	if (x == Double.POSITIVE_INFINITY) {
	    if (hiPrec != null) {
		hiPrec[0] = Double.POSITIVE_INFINITY;
	    }

	    return Double.POSITIVE_INFINITY;
	}

	/* Extract the exponent */
	int exp = (int) (bits &gt;&gt; 52) - 1023;

	if ((bits & 0x7ff0000000000000L) == 0) {
	    // Subnormal!
	    if (x == 0) {
		// Zero
		if (hiPrec != null) {
		    hiPrec[0] = Double.NEGATIVE_INFINITY;
		}

		return Double.NEGATIVE_INFINITY;
	    }

	    /* Normalize the subnormal number. */
	    bits &lt;&lt;= 1;
	    while ((bits & 0x0010000000000000L) == 0) {
		--exp;
		bits &lt;&lt;= 1;
	    }
	}

	if ((exp == -1 || exp == 0) && x &lt; 1.01 && x &gt; 0.99 && hiPrec == null) {
	    /* The normal method doesn't work well in the range [0.99, 1.01], so call do a straight
	    polynomial expansion in higer precision. */

	    /* Compute x - 1.0 and split it */
	    double xa = x - 1.0;
	    double xb = xa - x + 1.0;
	    double tmp = xa * HEX_40000000;
	    double aa = xa + tmp - tmp;
	    double ab = xa - aa;
	    xa = aa;
	    xb = ab;

	    final double[] lnCoef_last = LN_QUICK_COEF[LN_QUICK_COEF.length - 1];
	    double ya = lnCoef_last[0];
	    double yb = lnCoef_last[1];

	    for (int i = LN_QUICK_COEF.length - 2; i &gt;= 0; i--) {
		/* Multiply a = y * x */
		aa = ya * xa;
		ab = ya * xb + yb * xa + yb * xb;
		/* split, so now y = a */
		tmp = aa * HEX_40000000;
		ya = aa + tmp - tmp;
		yb = aa - ya + ab;

		/* Add  a = y + lnQuickCoef */
		final double[] lnCoef_i = LN_QUICK_COEF[i];
		aa = ya + lnCoef_i[0];
		ab = yb + lnCoef_i[1];
		/* Split y = a */
		tmp = aa * HEX_40000000;
		ya = aa + tmp - tmp;
		yb = aa - ya + ab;
	    }

	    /* Multiply a = y * x */
	    aa = ya * xa;
	    ab = ya * xb + yb * xa + yb * xb;
	    /* split, so now y = a */
	    tmp = aa * HEX_40000000;
	    ya = aa + tmp - tmp;
	    yb = aa - ya + ab;

	    return ya + yb;
	}

	// lnm is a log of a number in the range of 1.0 - 2.0, so 0 &lt;= lnm &lt; ln(2)
	final double[] lnm = lnMant.LN_MANT[(int) ((bits & 0x000ffc0000000000L) &gt;&gt; 42)];

	/*
	double epsilon = x / Double.longBitsToDouble(bits & 0xfffffc0000000000L);
	
	epsilon -= 1.0;
	 */

	// y is the most significant 10 bits of the mantissa
	//double y = Double.longBitsToDouble(bits & 0xfffffc0000000000L);
	//double epsilon = (x - y) / y;
	final double epsilon = (bits & 0x3ffffffffffL) / (TWO_POWER_52 + (bits & 0x000ffc0000000000L));

	double lnza = 0.0;
	double lnzb = 0.0;

	if (hiPrec != null) {
	    /* split epsilon -&gt; x */
	    double tmp = epsilon * HEX_40000000;
	    double aa = epsilon + tmp - tmp;
	    double ab = epsilon - aa;
	    double xa = aa;
	    double xb = ab;

	    /* Need a more accurate epsilon, so adjust the division. */
	    final double numer = bits & 0x3ffffffffffL;
	    final double denom = TWO_POWER_52 + (bits & 0x000ffc0000000000L);
	    aa = numer - xa * denom - xb * denom;
	    xb += aa / denom;

	    /* Remez polynomial evaluation */
	    final double[] lnCoef_last = LN_HI_PREC_COEF[LN_HI_PREC_COEF.length - 1];
	    double ya = lnCoef_last[0];
	    double yb = lnCoef_last[1];

	    for (int i = LN_HI_PREC_COEF.length - 2; i &gt;= 0; i--) {
		/* Multiply a = y * x */
		aa = ya * xa;
		ab = ya * xb + yb * xa + yb * xb;
		/* split, so now y = a */
		tmp = aa * HEX_40000000;
		ya = aa + tmp - tmp;
		yb = aa - ya + ab;

		/* Add  a = y + lnHiPrecCoef */
		final double[] lnCoef_i = LN_HI_PREC_COEF[i];
		aa = ya + lnCoef_i[0];
		ab = yb + lnCoef_i[1];
		/* Split y = a */
		tmp = aa * HEX_40000000;
		ya = aa + tmp - tmp;
		yb = aa - ya + ab;
	    }

	    /* Multiply a = y * x */
	    aa = ya * xa;
	    ab = ya * xb + yb * xa + yb * xb;

	    /* split, so now lnz = a */
	    /*
	    tmp = aa * 1073741824.0;
	    lnza = aa + tmp - tmp;
	    lnzb = aa - lnza + ab;
	     */
	    lnza = aa + ab;
	    lnzb = -(lnza - aa - ab);
	} else {
	    /* High precision not required.  Eval Remez polynomial
	    using standard double precision */
	    lnza = -0.16624882440418567;
	    lnza = lnza * epsilon + 0.19999954120254515;
	    lnza = lnza * epsilon + -0.2499999997677497;
	    lnza = lnza * epsilon + 0.3333333333332802;
	    lnza = lnza * epsilon + -0.5;
	    lnza = lnza * epsilon + 1.0;
	    lnza *= epsilon;
	}

	/* Relative sizes:
	 * lnzb     [0, 2.33E-10]
	 * lnm[1]   [0, 1.17E-7]
	 * ln2B*exp [0, 1.12E-4]
	 * lnza      [0, 9.7E-4]
	 * lnm[0]   [0, 0.692]
	 * ln2A*exp [0, 709]
	 */

	/* Compute the following sum:
	 * lnzb + lnm[1] + ln2B*exp + lnza + lnm[0] + ln2A*exp;
	 */

	//return lnzb + lnm[1] + ln2B*exp + lnza + lnm[0] + ln2A*exp;
	double a = LN_2_A * exp;
	double b = 0.0;
	double c = a + lnm[0];
	double d = -(c - a - lnm[0]);
	a = c;
	b += d;

	c = a + lnza;
	d = -(c - a - lnza);
	a = c;
	b += d;

	c = a + LN_2_B * exp;
	d = -(c - a - LN_2_B * exp);
	a = c;
	b += d;

	c = a + lnm[1];
	d = -(c - a - lnm[1]);
	a = c;
	b += d;

	c = a + lnzb;
	d = -(c - a - lnzb);
	a = c;
	b += d;

	if (hiPrec != null) {
	    hiPrec[0] = a;
	    hiPrec[1] = b;
	}

	return a + b;
    }

    /**
     * Internal helper method for exponential function.
     * @param x original argument of the exponential function
     * @param extra extra bits of precision on input (To Be Confirmed)
     * @param hiPrec extra bits of precision on output (To Be Confirmed)
     * @return exp(x)
     */
    private static double exp(double x, double extra, double[] hiPrec) {
	double intPartA;
	double intPartB;
	int intVal = (int) x;

	/* Lookup exp(floor(x)).
	 * intPartA will have the upper 22 bits, intPartB will have the lower
	 * 52 bits.
	 */
	if (x &lt; 0.0) {

	    // We don't check against intVal here as conversion of large negative double values
	    // may be affected by a JIT bug. Subsequent comparisons can safely use intVal
	    if (x &lt; -746d) {
		if (hiPrec != null) {
		    hiPrec[0] = 0.0;
		    hiPrec[1] = 0.0;
		}
		return 0.0;
	    }

	    if (intVal &lt; -709) {
		/* This will produce a subnormal output */
		final double result = exp(x + 40.19140625, extra, hiPrec) / 285040095144011776.0;
		if (hiPrec != null) {
		    hiPrec[0] /= 285040095144011776.0;
		    hiPrec[1] /= 285040095144011776.0;
		}
		return result;
	    }

	    if (intVal == -709) {
		/* exp(1.494140625) is nearly a machine number... */
		final double result = exp(x + 1.494140625, extra, hiPrec) / 4.455505956692756620;
		if (hiPrec != null) {
		    hiPrec[0] /= 4.455505956692756620;
		    hiPrec[1] /= 4.455505956692756620;
		}
		return result;
	    }

	    intVal--;

	} else {
	    if (intVal &gt; 709) {
		if (hiPrec != null) {
		    hiPrec[0] = Double.POSITIVE_INFINITY;
		    hiPrec[1] = 0.0;
		}
		return Double.POSITIVE_INFINITY;
	    }

	}

	intPartA = ExpIntTable.EXP_INT_TABLE_A[EXP_INT_TABLE_MAX_INDEX + intVal];
	intPartB = ExpIntTable.EXP_INT_TABLE_B[EXP_INT_TABLE_MAX_INDEX + intVal];

	/* Get the fractional part of x, find the greatest multiple of 2^-10 less than
	 * x and look up the exp function of it.
	 * fracPartA will have the upper 22 bits, fracPartB the lower 52 bits.
	 */
	final int intFrac = (int) ((x - intVal) * 1024.0);
	final double fracPartA = ExpFracTable.EXP_FRAC_TABLE_A[intFrac];
	final double fracPartB = ExpFracTable.EXP_FRAC_TABLE_B[intFrac];

	/* epsilon is the difference in x from the nearest multiple of 2^-10.  It
	 * has a value in the range 0 &lt;= epsilon &lt; 2^-10.
	 * Do the subtraction from x as the last step to avoid possible loss of precision.
	 */
	final double epsilon = x - (intVal + intFrac / 1024.0);

	/* Compute z = exp(epsilon) - 1.0 via a minimax polynomial.  z has
	full double precision (52 bits).  Since z &lt; 2^-10, we will have
	62 bits of precision when combined with the constant 1.  This will be
	used in the last addition below to get proper rounding. */

	/* Remez generated polynomial.  Converges on the interval [0, 2^-10], error
	is less than 0.5 ULP */
	double z = 0.04168701738764507;
	z = z * epsilon + 0.1666666505023083;
	z = z * epsilon + 0.5000000000042687;
	z = z * epsilon + 1.0;
	z = z * epsilon + -3.940510424527919E-20;

	/* Compute (intPartA+intPartB) * (fracPartA+fracPartB) by binomial
	expansion.
	tempA is exact since intPartA and intPartB only have 22 bits each.
	tempB will have 52 bits of precision.
	 */
	double tempA = intPartA * fracPartA;
	double tempB = intPartA * fracPartB + intPartB * fracPartA + intPartB * fracPartB;

	/* Compute the result.  (1+z)(tempA+tempB).  Order of operations is
	important.  For accuracy add by increasing size.  tempA is exact and
	much larger than the others.  If there are extra bits specified from the
	pow() function, use them. */
	final double tempC = tempB + tempA;

	// If tempC is positive infinite, the evaluation below could result in NaN,
	// because z could be negative at the same time.
	if (tempC == Double.POSITIVE_INFINITY) {
	    return Double.POSITIVE_INFINITY;
	}

	final double result;
	if (extra != 0.0) {
	    result = tempC * extra * z + tempC * extra + tempC * z + tempB + tempA;
	} else {
	    result = tempC * z + tempB + tempA;
	}

	if (hiPrec != null) {
	    // If requesting high precision
	    hiPrec[0] = tempA;
	    hiPrec[1] = tempC * extra * z + tempC * extra + tempC * z + tempB;
	}

	return result;
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
     * Returns the first argument with the sign of the second argument.
     * A NaN {@code sign} argument is treated as positive.
     *
     * @param magnitude the value to return
     * @param sign the sign for the returned value
     * @return the magnitude with the same sign as the {@code sign} argument
     */
    public static double copySign(double magnitude, double sign) {
	// The highest order bit is going to be zero if the
	// highest order bit of m and s is the same and one otherwise.
	// So (m^s) will be positive if both m and s have the same sign
	// and negative otherwise.
	final long m = Double.doubleToRawLongBits(magnitude); // don't care about NaN
	final long s = Double.doubleToRawLongBits(sign);
	if ((m ^ s) &gt;= 0) {
	    return magnitude;
	}
	return -magnitude; // flip sign
    }

    class Split {
	/** Mask used to extract exponent from double bits. */
	private static final long MASK_DOUBLE_EXPONENT = 0x7ff0000000000000L;
	/** Mask used to extract mantissa from double bits. */
	private static final long MASK_DOUBLE_MANTISSA = 0x000fffffffffffffL;
	/** Mask used to add implicit high order bit for normalized double. */
	private static final long IMPLICIT_HIGH_BIT = 0x0010000000000000L;
	/**
	* 0x40000000 - used to split a double into two parts, both with the low order bits cleared.
	* Equivalent to 2^30.
	*/
	private static final long HEX_40000000 = 0x40000000L;
	/** Coefficients for log, when input 0.99 &lt; x &lt; 1.01. */
	private static final double LN_QUICK_COEF[][] = { { 1.0, 5.669184079525E-24 }, { -0.25, -0.25 },
		{ 0.3333333134651184, 1.986821492305628E-8 }, { -0.25, -6.663542893624021E-14 },
		{ 0.19999998807907104, 1.1921056801463227E-8 }, { -0.1666666567325592, -7.800414592973399E-9 },
		{ 0.1428571343421936, 5.650007086920087E-9 }, { -0.12502530217170715, -7.44321345601866E-11 },
		{ 0.11113807559013367, 9.219544613762692E-9 }, };
	/** 2^52 - double numbers this large must be integral (no fraction) or NaN or Infinite */
	private static final double TWO_POWER_52 = 4503599627370496.0;
	/** Coefficients for log in the range of 1.0 &lt; x &lt; 1.0 + 2^-10. */
	private static final double LN_HI_PREC_COEF[][] = { { 1.0, -6.032174644509064E-23 }, { -0.25, -0.25 },
		{ 0.3333333134651184, 1.9868161777724352E-8 }, { -0.2499999701976776, -2.957007209750105E-8 },
		{ 0.19999954104423523, 1.5830993332061267E-10 }, { -0.16624879837036133, -2.6033824355191673E-8 } };
	/** log(2) (high bits). */
	private static final double LN_2_A = 0.693147063255310059;
	/** log(2) (low bits). */
	private static final double LN_2_B = 1.17304635250823482e-7;
	/** Index of exp(0) in the array of integer exponentials. */
	static final int EXP_INT_TABLE_MAX_INDEX = 750;
	/** Mask used to clear the non-sign part of a long. */
	private static final long MASK_NON_SIGN_LONG = 0x7fffffffffffffffl;

	/** Simple constructor.
	 * @param x number to split
	 */
	Split(final double x) {
	    full = x;
	    high = Double.longBitsToDouble(Double.doubleToRawLongBits(x) & ((-1L) &lt;&lt; 27));
	    low = x - high;
	}

	/** Computes this^e.
	 * @param e exponent (beware, here it MUST be &gt; 0; the only exclusion is Long.MIN_VALUE)
	 * @return d^e, split in high and low bits
	 * @since 3.6
	 */
	private Split pow(final long e) {

	    // prepare result
	    Split result = new Split(1);

	    // d^(2p)
	    Split d2p = new Split(full, high, low);

	    for (long p = e; p != 0; p &gt;&gt;&gt;= 1) {

		if ((p & 0x1) != 0) {
		    // accurate multiplication result = result * d^(2p) using Veltkamp TwoProduct algorithm
		    result = result.multiply(d2p);
		}

		// accurate squaring d^(2(p+1)) = d^(2p) * d^(2p) using Veltkamp TwoProduct algorithm
		d2p = d2p.multiply(d2p);

	    }

	    if (Double.isNaN(result.full)) {
		if (Double.isNaN(full)) {
		    return Split.NAN;
		} else {
		    // some intermediate numbers exceeded capacity,
		    // and the low order bits became NaN (because infinity - infinity = NaN)
		    if (FastMath.abs(full) &lt; 1) {
			return new Split(FastMath.copySign(0.0, full), 0.0);
		    } else if (full &lt; 0 && (e & 0x1) == 1) {
			return Split.NEGATIVE_INFINITY;
		    } else {
			return Split.POSITIVE_INFINITY;
		    }
		}
	    } else {
		return result;
	    }

	}

	/** Compute the reciprocal of the instance.
	 * @return reciprocal of the instance
	 */
	public Split reciprocal() {

	    final double approximateInv = 1.0 / full;
	    final Split splitInv = new Split(approximateInv);

	    // if 1.0/d were computed perfectly, remultiplying it by d should give 1.0
	    // we want to estimate the error so we can fix the low order bits of approximateInvLow
	    // beware the following expressions must NOT be simplified, they rely on floating point arithmetic properties
	    final Split product = multiply(splitInv);
	    final double error = (product.high - 1) + product.low;

	    // better accuracy estimate of reciprocal
	    return Double.isNaN(error) ? splitInv : new Split(splitInv.high, splitInv.low - error / full);

	}

	/** Simple constructor.
	 * @param full full number
	 * @param high high order bits
	 * @param low low order bits
	 */
	Split(final double full, final double high, final double low) {
	    this.full = full;
	    this.high = high;
	    this.low = low;
	}

	/** Multiply the instance by another one.
	 * @param b other instance to multiply by
	 * @return product
	 */
	public Split multiply(final Split b) {
	    // beware the following expressions must NOT be simplified, they rely on floating point arithmetic properties
	    final Split mulBasic = new Split(full * b.full);
	    final double mulError = low * b.low - (((mulBasic.full - high * b.high) - low * b.high) - high * b.low);
	    return new Split(mulBasic.high, mulBasic.low + mulError);
	}

	/** Simple constructor.
	 * @param high high order bits
	 * @param low low order bits
	 */
	Split(final double high, final double low) {
	    this(high == 0.0
		    ? (low == 0.0 && Double.doubleToRawLongBits(high) == Long.MIN_VALUE /* negative zero */ ? -0.0
			    : low)
		    : high + low, high, low);
	}

    }

}

