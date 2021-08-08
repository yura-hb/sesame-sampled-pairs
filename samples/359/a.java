import org.apache.commons.lang3.Validate;

class Fraction extends Number implements Comparable&lt;Fraction&gt; {
    /**
     * &lt;p&gt;Multiplies the value of this fraction by another, returning the
     * result in reduced form.&lt;/p&gt;
     *
     * @param fraction  the fraction to multiply by, must not be &lt;code&gt;null&lt;/code&gt;
     * @return a &lt;code&gt;Fraction&lt;/code&gt; instance with the resulting values
     * @throws IllegalArgumentException if the fraction is &lt;code&gt;null&lt;/code&gt;
     * @throws ArithmeticException if the resulting numerator or denominator exceeds
     *  &lt;code&gt;Integer.MAX_VALUE&lt;/code&gt;
     */
    public Fraction multiplyBy(final Fraction fraction) {
	Validate.isTrue(fraction != null, "The fraction must not be null");
	if (numerator == 0 || fraction.numerator == 0) {
	    return ZERO;
	}
	// knuth 4.5.1
	// make sure we don't overflow unless the result *must* overflow.
	final int d1 = greatestCommonDivisor(numerator, fraction.denominator);
	final int d2 = greatestCommonDivisor(fraction.numerator, denominator);
	return getReducedFraction(mulAndCheck(numerator / d1, fraction.numerator / d2),
		mulPosAndCheck(denominator / d2, fraction.denominator / d1));
    }

    /**
     * The numerator number part of the fraction (the three in three sevenths).
     */
    private final int numerator;
    /**
     * &lt;code&gt;Fraction&lt;/code&gt; representation of 0.
     */
    public static final Fraction ZERO = new Fraction(0, 1);
    /**
     * The denominator number part of the fraction (the seven in three sevenths).
     */
    private final int denominator;

    /**
     * &lt;p&gt;Gets the greatest common divisor of the absolute value of
     * two numbers, using the "binary gcd" method which avoids
     * division and modulo operations.  See Knuth 4.5.2 algorithm B.
     * This algorithm is due to Josef Stein (1961).&lt;/p&gt;
     *
     * @param u  a non-zero number
     * @param v  a non-zero number
     * @return the greatest common divisor, never zero
     */
    private static int greatestCommonDivisor(int u, int v) {
	// From Commons Math:
	if (u == 0 || v == 0) {
	    if (u == Integer.MIN_VALUE || v == Integer.MIN_VALUE) {
		throw new ArithmeticException("overflow: gcd is 2^31");
	    }
	    return Math.abs(u) + Math.abs(v);
	}
	// if either operand is abs 1, return 1:
	if (Math.abs(u) == 1 || Math.abs(v) == 1) {
	    return 1;
	}
	// keep u and v negative, as negative integers range down to
	// -2^31, while positive numbers can only be as large as 2^31-1
	// (i.e. we can't necessarily negate a negative number without
	// overflow)
	if (u &gt; 0) {
	    u = -u;
	} // make u negative
	if (v &gt; 0) {
	    v = -v;
	} // make v negative
	  // B1. [Find power of 2]
	int k = 0;
	while ((u & 1) == 0 && (v & 1) == 0 && k &lt; 31) { // while u and v are both even...
	    u /= 2;
	    v /= 2;
	    k++; // cast out twos.
	}
	if (k == 31) {
	    throw new ArithmeticException("overflow: gcd is 2^31");
	}
	// B2. Initialize: u and v have been divided by 2^k and at least
	// one is odd.
	int t = (u & 1) == 1 ? v : -(u / 2)/* B3 */;
	// t negative: u was odd, v may be even (t replaces v)
	// t positive: u was even, v is odd (t replaces u)
	do {
	    /* assert u&lt;0 && v&lt;0; */
	    // B4/B3: cast out twos from t.
	    while ((t & 1) == 0) { // while t is even..
		t /= 2; // cast out twos
	    }
	    // B5 [reset max(u,v)]
	    if (t &gt; 0) {
		u = -t;
	    } else {
		v = t;
	    }
	    // B6/B3. at this point both u and v should be odd.
	    t = (v - u) / 2;
	    // |u| larger: t positive (replace u)
	    // |v| larger: t negative (replace v)
	} while (t != 0);
	return -u * (1 &lt;&lt; k); // gcd is u*2^k
    }

    /**
     * Multiply two integers, checking for overflow.
     *
     * @param x a factor
     * @param y a factor
     * @return the product &lt;code&gt;x*y&lt;/code&gt;
     * @throws ArithmeticException if the result can not be represented as
     *                             an int
     */
    private static int mulAndCheck(final int x, final int y) {
	final long m = (long) x * (long) y;
	if (m &lt; Integer.MIN_VALUE || m &gt; Integer.MAX_VALUE) {
	    throw new ArithmeticException("overflow: mul");
	}
	return (int) m;
    }

    /**
     *  Multiply two non-negative integers, checking for overflow.
     *
     * @param x a non-negative factor
     * @param y a non-negative factor
     * @return the product &lt;code&gt;x*y&lt;/code&gt;
     * @throws ArithmeticException if the result can not be represented as
     * an int
     */
    private static int mulPosAndCheck(final int x, final int y) {
	/* assert x&gt;=0 && y&gt;=0; */
	final long m = (long) x * (long) y;
	if (m &gt; Integer.MAX_VALUE) {
	    throw new ArithmeticException("overflow: mulPos");
	}
	return (int) m;
    }

    /**
     * &lt;p&gt;Creates a reduced &lt;code&gt;Fraction&lt;/code&gt; instance with the 2 parts
     * of a fraction Y/Z.&lt;/p&gt;
     *
     * &lt;p&gt;For example, if the input parameters represent 2/4, then the created
     * fraction will be 1/2.&lt;/p&gt;
     *
     * &lt;p&gt;Any negative signs are resolved to be on the numerator.&lt;/p&gt;
     *
     * @param numerator  the numerator, for example the three in 'three sevenths'
     * @param denominator  the denominator, for example the seven in 'three sevenths'
     * @return a new fraction instance, with the numerator and denominator reduced
     * @throws ArithmeticException if the denominator is &lt;code&gt;zero&lt;/code&gt;
     */
    public static Fraction getReducedFraction(int numerator, int denominator) {
	if (denominator == 0) {
	    throw new ArithmeticException("The denominator must not be zero");
	}
	if (numerator == 0) {
	    return ZERO; // normalize zero.
	}
	// allow 2^k/-2^31 as a valid fraction (where k&gt;0)
	if (denominator == Integer.MIN_VALUE && (numerator & 1) == 0) {
	    numerator /= 2;
	    denominator /= 2;
	}
	if (denominator &lt; 0) {
	    if (numerator == Integer.MIN_VALUE || denominator == Integer.MIN_VALUE) {
		throw new ArithmeticException("overflow: can't negate");
	    }
	    numerator = -numerator;
	    denominator = -denominator;
	}
	// simplify fraction.
	final int gcd = greatestCommonDivisor(numerator, denominator);
	numerator /= gcd;
	denominator /= gcd;
	return new Fraction(numerator, denominator);
    }

    /**
     * &lt;p&gt;Constructs a &lt;code&gt;Fraction&lt;/code&gt; instance with the 2 parts
     * of a fraction Y/Z.&lt;/p&gt;
     *
     * @param numerator  the numerator, for example the three in 'three sevenths'
     * @param denominator  the denominator, for example the seven in 'three sevenths'
     */
    private Fraction(final int numerator, final int denominator) {
	super();
	this.numerator = numerator;
	this.denominator = denominator;
    }

}

