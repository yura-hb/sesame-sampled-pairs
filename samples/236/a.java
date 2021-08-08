import static com.google.common.primitives.UnsignedInts.INT_MASK;

class UnsignedInteger extends Number implements Comparable&lt;UnsignedInteger&gt; {
    /**
    * Returns an {@code UnsignedInteger} that is equal to {@code value}, if possible. The inverse
    * operation of {@link #longValue()}.
    */
    public static UnsignedInteger valueOf(long value) {
	checkArgument((value & INT_MASK) == value, "value (%s) is outside the range for an unsigned integer value",
		value);
	return fromIntBits((int) value);
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

