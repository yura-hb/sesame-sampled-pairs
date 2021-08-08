class UnsignedLong extends Number implements Comparable&lt;UnsignedLong&gt;, Serializable {
    /**
    * Returns an {@code UnsignedLong} representing the same value as the specified {@code long}.
    *
    * @throws IllegalArgumentException if {@code value} is negative
    * @since 14.0
    */
    @CanIgnoreReturnValue
    public static UnsignedLong valueOf(long value) {
	checkArgument(value &gt;= 0, "value (%s) is outside the range for an unsigned long value", value);
	return fromLongBits(value);
    }

    private final long value;

    /**
    * Returns an {@code UnsignedLong} corresponding to a given bit representation. The argument is
    * interpreted as an unsigned 64-bit value. Specifically, the sign bit of {@code bits} is
    * interpreted as a normal bit, and all other bits are treated as usual.
    *
    * &lt;p&gt;If the argument is nonnegative, the returned result will be equal to {@code bits},
    * otherwise, the result will be equal to {@code 2^64 + bits}.
    *
    * &lt;p&gt;To represent decimal constants less than {@code 2^63}, consider {@link #valueOf(long)}
    * instead.
    *
    * @since 14.0
    */
    public static UnsignedLong fromLongBits(long bits) {
	// TODO(lowasser): consider caching small values, like Long.valueOf
	return new UnsignedLong(bits);
    }

    private UnsignedLong(long value) {
	this.value = value;
    }

}

