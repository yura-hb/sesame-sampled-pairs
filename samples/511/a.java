import java.math.BigInteger;

class Rational implements Cloneable {
    /**
     * Subtract an integer.
     *
     * @param val the number to be subtracted from this
     * @return this - val.
     */
    public Rational subtract(int val) {
	Rational val2 = new Rational(val, 1);
	return (subtract(val2));
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
     * ctor from a numerator and denominator.
     *
     * @param a the numerator.
     * @param b the denominator.
     */
    public Rational(int a, int b) {
	this(BigInteger.valueOf(a), BigInteger.valueOf(b));
    }

    /**
     * Subtract another fraction.
     * 7
     *
     * @param val the number to be subtracted from this
     * @return this - val.
     */
    public Rational subtract(Rational val) {
	Rational val2 = val.negate();
	return (add(val2));
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
     * Compute the negative.
     *
     * @return -this.
     */
    public Rational negate() {
	return (new Rational(a.negate(), b));
    }

    /**
     * Add another fraction.
     *
     * @param val The number to be added
     * @return this+val.
     */
    public Rational add(Rational val) {
	BigInteger num = a.multiply(val.b).add(b.multiply(val.a));
	BigInteger deno = b.multiply(val.b);
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

