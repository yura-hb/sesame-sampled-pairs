class Fraction extends Number implements Comparable&lt;Fraction&gt; {
    /**
     * &lt;p&gt;Gets the fraction as a &lt;code&gt;float&lt;/code&gt;. This calculates the fraction
     * as the numerator divided by denominator.&lt;/p&gt;
     *
     * @return the fraction as a &lt;code&gt;float&lt;/code&gt;
     */
    @Override
    public float floatValue() {
	return (float) numerator / (float) denominator;
    }

    /**
     * The numerator number part of the fraction (the three in three sevenths).
     */
    private final int numerator;
    /**
     * The denominator number part of the fraction (the seven in three sevenths).
     */
    private final int denominator;

}

