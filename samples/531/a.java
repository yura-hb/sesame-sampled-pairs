class Decimal64 extends Number implements RealFieldElement&lt;Decimal64&gt;, Comparable&lt;Decimal64&gt; {
    /**
     * {@inheritDoc}
     *
     * The current implementation returns the same value as
     * &lt;center&gt; {@code new Double(this.doubleValue()).compareTo(new
     * Double(o.doubleValue()))} &lt;/center&gt;
     *
     * @see Double#compareTo(Double)
     */
    @Override
    public int compareTo(final Decimal64 o) {
	return Double.compare(this.value, o.value);
    }

    /** The primitive {@code double} value of this object. */
    private final double value;

}

