import java.math.BigInteger;
import org.apache.commons.math4.util.FastMath;

class BigFraction extends Number implements FieldElement&lt;BigFraction&gt;, Comparable&lt;BigFraction&gt;, Serializable {
    /**
     * &lt;p&gt;
     * Gets the fraction as a {@code float}. This calculates the fraction as
     * the numerator divided by denominator.
     * &lt;/p&gt;
     *
     * @return the fraction as a {@code float}.
     * @see java.lang.Number#floatValue()
     */
    @Override
    public float floatValue() {
	float result = numerator.floatValue() / denominator.floatValue();
	if (Double.isNaN(result)) {
	    // Numerator and/or denominator must be out of range:
	    // Calculate how far to shift them to put them in range.
	    int shift = FastMath.max(numerator.bitLength(), denominator.bitLength())
		    - FastMath.getExponent(Float.MAX_VALUE);
	    result = numerator.shiftRight(shift).floatValue() / denominator.shiftRight(shift).floatValue();
	}
	return result;
    }

    /** The numerator. */
    private final BigInteger numerator;
    /** The denominator. */
    private final BigInteger denominator;

}

