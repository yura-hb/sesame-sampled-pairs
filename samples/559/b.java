class UnsignedInteger extends Number implements Comparable&lt;UnsignedInteger&gt; {
    /**
    * Returns the result of dividing this by {@code val}.
    *
    * @throws ArithmeticException if {@code val} is zero
    * @since 14.0
    */
    public UnsignedInteger dividedBy(UnsignedInteger val) {
	return fromIntBits(UnsignedInts.divide(value, checkNotNull(val).value));
    }

    private final int value;

    /**
    * Returns an {@code UnsignedInteger} corresponding to a given bit representation. The argument is
    * interpreted as an unsigned 32-bit value. Specifically, the sign bit of {@code bits} is
    * interpreted as a normal bit, and all other bits are treated as usual.
    *
    * &lt;p&gt;If the argument is nonnegative, the returned result will be equal to {@code bits},
    * otherwise, the result will be equal to {@code 2^32 + bits}.
    *
    * &lt;p&gt;To represent unsigned decimal constants, consider {@link #valueOf(long)} instead.
    *
    * @since 14.0
    */
    public static UnsignedInteger fromIntBits(int bits) {
	return new UnsignedInteger(bits);
    }

    private UnsignedInteger(int value) {
	// GWT doesn't consistently overflow values to make them 32-bit, so we need to force it.
	this.value = value & 0xffffffff;
    }

}

