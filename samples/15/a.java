class Fraction extends Number implements Comparable&lt;Fraction&gt; {
    /**
     * &lt;p&gt;Creates a &lt;code&gt;Fraction&lt;/code&gt; instance with the 2 parts
     * of a fraction Y/Z.&lt;/p&gt;
     *
     * &lt;p&gt;Any negative signs are resolved to be on the numerator.&lt;/p&gt;
     *
     * @param numerator  the numerator, for example the three in 'three sevenths'
     * @param denominator  the denominator, for example the seven in 'three sevenths'
     * @return a new fraction instance
     * @throws ArithmeticException if the denominator is &lt;code&gt;zero&lt;/code&gt;
     * or the denominator is {@code negative} and the numerator is {@code Integer#MIN_VALUE}
     */
    public static Fraction getFraction(int numerator, int denominator) {
	if (denominator == 0) {
	    throw new ArithmeticException("The denominator must not be zero");
	}
	if (denominator &lt; 0) {
	    if (numerator == Integer.MIN_VALUE || denominator == Integer.MIN_VALUE) {
		throw new ArithmeticException("overflow: can't negate");
	    }
	    numerator = -numerator;
	    denominator = -denominator;
	}
	return new Fraction(numerator, denominator);
    }

    /**
     * The numerator number part of the fraction (the three in three sevenths).
     */
    private final int numerator;
    /**
     * The denominator number part of the fraction (the seven in three sevenths).
     */
    private final int denominator;

    /**
     * &lt;p&gt;Constructs a &lt;code&gt;Fraction&lt;/code&gt; instance with the 2 parts
     * of a fraction Y/Z.&lt;/p&gt;
     *
     * @param numerator  the numerator, for example the three in 'three sevenths'
     * @param denominator  the denominator, for example the seven in 'three sevenths'
     */
    private Fraction(final int numerator, final int denominator) {
	super();
	this.numerator = numerator;
	this.denominator = denominator;
    }

}

