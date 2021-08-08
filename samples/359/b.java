import java.math.BigInteger;
import org.apache.commons.math4.exception.ZeroException;
import org.apache.commons.math4.exception.util.LocalizedFormats;
import org.apache.commons.math4.util.MathUtils;

class BigFraction extends Number implements FieldElement&lt;BigFraction&gt;, Comparable&lt;BigFraction&gt;, Serializable {
    /**
     * &lt;p&gt;
     * Multiplies the value of this fraction by another, returning the result in
     * reduced form.
     * &lt;/p&gt;
     *
     * @param fraction Fraction to multiply by, must not be {@code null}.
     * @return a {@link BigFraction} instance with the resulting values.
     * @throws NullArgumentException if {@code fraction} is {@code null}.
     */
    @Override
    public BigFraction multiply(final BigFraction fraction) {
	MathUtils.checkNotNull(fraction, LocalizedFormats.FRACTION);
	if (numerator.signum() == 0 || fraction.numerator.signum() == 0) {
	    return ZERO;
	}
	return new BigFraction(numerator.multiply(fraction.numerator), denominator.multiply(fraction.denominator));
    }

    /** The numerator. */
    private final BigInteger numerator;
    /** A fraction representing "0". */
    public static final BigFraction ZERO = new BigFraction(0);
    /** The denominator. */
    private final BigInteger denominator;

    /**
     * Create a {@link BigFraction} given the numerator and denominator as
     * {@code BigInteger}. The {@link BigFraction} is reduced to lowest terms.
     *
     * @param num the numerator, must not be {@code null}.
     * @param den the denominator, must not be {@code null}.
     * @throws ZeroException if the denominator is zero.
     * @throws NullArgumentException if either of the arguments is null
     */
    public BigFraction(BigInteger num, BigInteger den) {
	MathUtils.checkNotNull(num, LocalizedFormats.NUMERATOR);
	MathUtils.checkNotNull(den, LocalizedFormats.DENOMINATOR);
	if (den.signum() == 0) {
	    throw new ZeroException(LocalizedFormats.ZERO_DENOMINATOR);
	}
	if (num.signum() == 0) {
	    numerator = BigInteger.ZERO;
	    denominator = BigInteger.ONE;
	} else {

	    // reduce numerator and denominator by greatest common denominator
	    final BigInteger gcd = num.gcd(den);
	    if (BigInteger.ONE.compareTo(gcd) &lt; 0) {
		num = num.divide(gcd);
		den = den.divide(gcd);
	    }

	    // move sign to numerator
	    if (den.signum() == -1) {
		num = num.negate();
		den = den.negate();
	    }

	    // store the values in the final fields
	    numerator = num;
	    denominator = den;

	}
    }

}

