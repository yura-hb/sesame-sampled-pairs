import org.apache.commons.math4.exception.MathArithmeticException;
import org.apache.commons.math4.exception.util.LocalizedFormats;

class Fraction extends Number implements FieldElement&lt;Fraction&gt;, Comparable&lt;Fraction&gt;, Serializable {
    /**
     * Subtract an integer from the fraction.
     * @param i the {@code integer} to subtract.
     * @return this - i
     */
    public Fraction subtract(final int i) {
	return new Fraction(numerator - i * denominator, denominator);
    }

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

