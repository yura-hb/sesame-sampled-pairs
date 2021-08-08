import java.math.BigInteger;

class Rational implements Cloneable {
    /**
     * floor(): the nearest integer not greater than this.
     *
     * @return The integer rounded towards negative infinity.
     */
    public BigInteger floor() {
	/* is already integer: return the numerator
	 */
	if (b.compareTo(BigInteger.ONE) == 0) {
	    return a;
	} else if (a.compareTo(BigInteger.ZERO) &gt; 0) {
	    return a.divide(b);
	} else {
	    return a.divide(b).subtract(BigInteger.ONE);
	}
    }

    /**
     * denominator
     */
    BigInteger b;
    /**
     * numerator
     */
    BigInteger a;

}

