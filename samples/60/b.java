import org.apache.commons.math4.exception.MathArithmeticException;
import org.apache.commons.math4.exception.NullArgumentException;
import org.apache.commons.math4.exception.util.LocalizedFormats;

class Fraction extends Number implements FieldElement&lt;Fraction&gt;, Comparable&lt;Fraction&gt;, Serializable {
    /**
     * &lt;p&gt;Divide the value of this fraction by another.&lt;/p&gt;
     *
     * @param fraction  the fraction to divide by, must not be {@code null}
     * @return a {@code Fraction} instance with the resulting values
     * @throws IllegalArgumentException if the fraction is {@code null}
     * @throws MathArithmeticException if the fraction to divide by is zero
     * @throws MathArithmeticException if the resulting numerator or denominator exceeds
     *  {@code Integer.MAX_VALUE}
     */
    @Override
    public Fraction divide(Fraction fraction) {
	if (fraction == null) {
	    throw new NullArgumentException(LocalizedFormats.FRACTION);
	}
	if (fraction.numerator == 0) {
	    throw new MathArithmeticException(LocalizedFormats.ZERO_FRACTION_TO_DIVIDE_BY, fraction.numerator,
		    fraction.denominator);
	}
	return multiply(fraction.reciprocal());
    }

    /** The numerator. */
    private final int numerator;
    /** The denominator. */
    private final int denominator;
    /** A fraction representing "0". */
    public static final Fraction ZERO = new Fraction(0, 1);

    /**
     * Return the multiplicative inverse of this fraction.
     * @return the reciprocal fraction
     */
    @Override
    public Fraction reciprocal() {
	return new Fraction(denominator, numerator);
    }

    /**
     * &lt;p&gt;Multiplies the value of this fraction by another, returning the
     * result in reduced form.&lt;/p&gt;
     *
     * @param fraction  the fraction to multiply by, must not be {@code null}
     * @return a {@code Fraction} instance with the resulting values
     * @throws NullArgumentException if the fraction is {@code null}
     * @throws MathArithmeticException if the resulting numerator or denominator exceeds
     *  {@code Integer.MAX_VALUE}
     */
    @Override
    public Fraction multiply(Fraction fraction) {
	if (fraction == null) {
	    throw new NullArgumentException(LocalizedFormats.FRACTION);
	}
	if (numerator == 0 || fraction.numerator == 0) {
	    return ZERO;
	}
	// knuth 4.5.1
	// make sure we don't overflow unless the result *must* overflow.
	int d1 = ArithmeticUtils.gcd(numerator, fraction.denominator);
	int d2 = ArithmeticUtils.gcd(fraction.numerator, denominator);
	return getReducedFraction(ArithmeticUtils.mulAndCheck(numerator / d1, fraction.numerator / d2),
		ArithmeticUtils.mulAndCheck(denominator / d2, fraction.denominator / d1));
    }

    /**
     * Create a fraction given the numerator and denominator.  The fraction is
     * reduced to lowest terms.
     * @param num the numerator.
     * @param den the denominator.
     * @throws MathArithmeticException if the denominator is {@code zero}
     */
    public Fraction(int num, int den) {
	if (den == 0) {
	    throw new MathArithmeticException(LocalizedFormats.ZERO_DENOMINATOR_IN_FRACTION, num, den);
	}
	if (den &lt; 0) {
	    if (num == Integer.MIN_VALUE || den == Integer.MIN_VALUE) {
		throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_FRACTION, num, den);
	    }
	    num = -num;
	    den = -den;
	}
	// reduce numerator and denominator by greatest common denominator.
	final int d = ArithmeticUtils.gcd(num, den);
	if (d &gt; 1) {
	    num /= d;
	    den /= d;
	}

	// move sign to numerator.
	if (den &lt; 0) {
	    num = -num;
	    den = -den;
	}
	this.numerator = num;
	this.denominator = den;
    }

    /**
     * &lt;p&gt;Creates a {@code Fraction} instance with the 2 parts
     * of a fraction Y/Z.&lt;/p&gt;
     *
     * &lt;p&gt;Any negative signs are resolved to be on the numerator.&lt;/p&gt;
     *
     * @param numerator  the numerator, for example the three in 'three sevenths'
     * @param denominator  the denominator, for example the seven in 'three sevenths'
     * @return a new fraction instance, with the numerator and denominator reduced
     * @throws MathArithmeticException if the denominator is {@code zero}
     */
    public static Fraction getReducedFraction(int numerator, int denominator) {
	if (denominator == 0) {
	    throw new MathArithmeticException(LocalizedFormats.ZERO_DENOMINATOR_IN_FRACTION, numerator, denominator);
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
		throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_FRACTION, numerator, denominator);
	    }
	    numerator = -numerator;
	    denominator = -denominator;
	}
	// simplify fraction.
	int gcd = ArithmeticUtils.gcd(numerator, denominator);
	numerator /= gcd;
	denominator /= gcd;
	return new Fraction(numerator, denominator);
    }

}

