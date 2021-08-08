class HashCodeBuilder implements Builder&lt;Integer&gt; {
    /**
     * &lt;p&gt;
     * Append a &lt;code&gt;hashCode&lt;/code&gt; for a &lt;code&gt;byte&lt;/code&gt;.
     * &lt;/p&gt;
     *
     * @param value
     *            the byte to add to the &lt;code&gt;hashCode&lt;/code&gt;
     * @return this
     */
    public HashCodeBuilder append(final byte value) {
	iTotal = iTotal * iConstant + value;
	return this;
    }

    /**
     * Running total of the hashCode.
     */
    private int iTotal = 0;
    /**
     * Constant to use in building the hashCode.
     */
    private final int iConstant;

}

