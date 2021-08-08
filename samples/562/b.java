import java.math.BigInteger;
import org.apache.commons.math4.exception.ZeroException;
import org.apache.commons.math4.exception.util.LocalizedFormats;
import org.apache.commons.math4.util.MathUtils;

class BigFraction extends Number implements FieldElement&lt;BigFraction&gt;, Comparable&lt;BigFraction&gt;, Serializable {
    /**
     * &lt;p&gt;
     * Creates a &lt;code&gt;BigFraction&lt;/code&gt; instance with the 2 parts of a fraction
     * Y/Z.
     * &lt;/p&gt;
     *
     * &lt;p&gt;
     * Any negative signs are resolved to be on the numerator.
     * &lt;/p&gt;
     *
     * @param numerator
     *            the numerator, for example the three in 'three sevenths'.
     * @param denominator
     *            the denominator, for example the seven in 'three sevenths'.
     * @return a new fraction instance, with the numerator and denominator
     *         reduced.
     * @throws ArithmeticException
     *             if the denominator is &lt;code&gt;zero&lt;/code&gt;.
     */
    public static BigFraction getReducedFraction(final int numerator, final int denominator) {
	if (numerator == 0) {
	    return ZERO; // normalize zero.
	}

	return new BigFraction(numerator, denominator);
    }

    /** A fraction representing "0". */
    public static final BigFraction ZERO = new BigFraction(0);
    /** The numerator. */
    private final BigInteger numerator;
    /** The denominator. */
    private final BigInteger denominator;

    /**
     * &lt;p&gt;
     * Create a {@link BigFraction} given the numerator and denominator as simple
     * {@code int}. The {@link BigFraction} is reduced to lowest terms.
     * &lt;/p&gt;
     *
     * @param num
     *            the numerator.
     * @param den
     *            the denominator.
     */
    public BigFraction(final int num, final int den) {
	this(BigInteger.valueOf(num), BigInteger.valueOf(den));
    }

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

