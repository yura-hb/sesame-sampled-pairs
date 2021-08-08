import org.apache.commons.math4.exception.MathArithmeticException;
import org.apache.commons.math4.exception.util.LocalizedFormats;

class Fraction extends Number implements FieldElement&lt;Fraction&gt;, Comparable&lt;Fraction&gt;, Serializable {
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

    /** A fraction representing "0". */
    public static final Fraction ZERO = new Fraction(0, 1);
    /** The numerator. */
    private final int numerator;
    /** The denominator. */
    private final int denominator;

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

}

