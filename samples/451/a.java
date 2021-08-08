import java.math.BigInteger;

class Rational implements Cloneable {
    /**
     * Multiply by an integer.
     *
     * @param val a second number.
     * @return the product of this with the value.
     */
    public Rational multiply(final int val) {
	BigInteger tmp = BigInteger.valueOf(val);
	return multiply(tmp);
    }

    /**
     * numerator
     */
    BigInteger a;
    /**
     * denominator
     */
    BigInteger b;

    /**
     * Multiply by a BigInteger.
     *
     * @param val a second number.
     * @return the product of this with the value.
     */
    public Rational multiply(final BigInteger val) {
	Rational val2 = new Rational(val, BigInteger.ONE);
	return (multiply(val2));
    }

    /**
     * ctor from a numerator and denominator.
     *
     * @param a the numerator.
     * @param b the denominator.
     */
    public Rational(BigInteger a, BigInteger b) {
	this.a = a;
	this.b = b;
	normalize();
    }

    /**
     * Multiply by another fraction.
     *
     * @param val a second rational number.
     * @return the product of this with the val.
     */
    public Rational multiply(final Rational val) {
	BigInteger num = a.multiply(val.a);
	BigInteger deno = b.multiply(val.b);
	/* Normalization to an coprime format will be done inside
	 * the ctor() and is not duplicated here.
	 */
	return (new Rational(num, deno));
    }

    /**
     * Normalize to coprime numerator and denominator.
     * Also copy a negative sign of the denominator to the numerator.
     */
    protected void normalize() {
	/* compute greatest common divisor of numerator and denominator
	 */
	final BigInteger g = a.gcd(b);
	if (g.compareTo(BigInteger.ONE) &gt; 0) {
	    a = a.divide(g);
	    b = b.divide(g);
	}
	if (b.compareTo(BigInteger.ZERO) == -1) {
	    a = a.negate();
	    b = b.negate();
	}
    }

}

