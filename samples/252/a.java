import java.math.BigInteger;
import org.apache.commons.math4.util.FastMath;

class BigFraction extends Number implements FieldElement&lt;BigFraction&gt;, Comparable&lt;BigFraction&gt;, Serializable {
    /**
     * &lt;p&gt;
     * Returns a &lt;code&gt;double&lt;/code&gt; whose value is
     * &lt;tt&gt;(this&lt;sup&gt;exponent&lt;/sup&gt;)&lt;/tt&gt;, returning the result in reduced form.
     * &lt;/p&gt;
     *
     * @param exponent
     *            exponent to which this &lt;code&gt;BigFraction&lt;/code&gt; is to be raised.
     * @return &lt;tt&gt;this&lt;sup&gt;exponent&lt;/sup&gt;&lt;/tt&gt;.
     */
    public double pow(final double exponent) {
	return FastMath.pow(numerator.doubleValue(), exponent) / FastMath.pow(denominator.doubleValue(), exponent);
    }

    /** The numerator. */
    private final BigInteger numerator;
    /** The denominator. */
    private final BigInteger denominator;

}

