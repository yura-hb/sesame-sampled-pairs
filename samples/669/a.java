class Double extends Number implements Comparable&lt;Double&gt; {
    /**
     * Returns a hash code for a {@code double} value; compatible with
     * {@code Double.hashCode()}.
     *
     * @param value the value to hash
     * @return a hash code value for a {@code double} value.
     * @since 1.8
     */
    public static int hashCode(double value) {
	long bits = doubleToLongBits(value);
	return (int) (bits ^ (bits &gt;&gt;&gt; 32));
    }

    /**
     * Returns a representation of the specified floating-point value
     * according to the IEEE 754 floating-point "double
     * format" bit layout.
     *
     * &lt;p&gt;Bit 63 (the bit that is selected by the mask
     * {@code 0x8000000000000000L}) represents the sign of the
     * floating-point number. Bits
     * 62-52 (the bits that are selected by the mask
     * {@code 0x7ff0000000000000L}) represent the exponent. Bits 51-0
     * (the bits that are selected by the mask
     * {@code 0x000fffffffffffffL}) represent the significand
     * (sometimes called the mantissa) of the floating-point number.
     *
     * &lt;p&gt;If the argument is positive infinity, the result is
     * {@code 0x7ff0000000000000L}.
     *
     * &lt;p&gt;If the argument is negative infinity, the result is
     * {@code 0xfff0000000000000L}.
     *
     * &lt;p&gt;If the argument is NaN, the result is
     * {@code 0x7ff8000000000000L}.
     *
     * &lt;p&gt;In all cases, the result is a {@code long} integer that, when
     * given to the {@link #longBitsToDouble(long)} method, will produce a
     * floating-point value the same as the argument to
     * {@code doubleToLongBits} (except all NaN values are
     * collapsed to a single "canonical" NaN value).
     *
     * @param   value   a {@code double} precision floating-point number.
     * @return the bits that represent the floating-point number.
     */
    @HotSpotIntrinsicCandidate
    public static long doubleToLongBits(double value) {
	if (!isNaN(value)) {
	    return doubleToRawLongBits(value);
	}
	return 0x7ff8000000000000L;
    }

    /**
     * Returns {@code true} if the specified number is a
     * Not-a-Number (NaN) value, {@code false} otherwise.
     *
     * @param   v   the value to be tested.
     * @return  {@code true} if the value of the argument is NaN;
     *          {@code false} otherwise.
     */
    public static boolean isNaN(double v) {
	return (v != v);
    }

    /**
     * Returns a representation of the specified floating-point value
     * according to the IEEE 754 floating-point "double
     * format" bit layout, preserving Not-a-Number (NaN) values.
     *
     * &lt;p&gt;Bit 63 (the bit that is selected by the mask
     * {@code 0x8000000000000000L}) represents the sign of the
     * floating-point number. Bits
     * 62-52 (the bits that are selected by the mask
     * {@code 0x7ff0000000000000L}) represent the exponent. Bits 51-0
     * (the bits that are selected by the mask
     * {@code 0x000fffffffffffffL}) represent the significand
     * (sometimes called the mantissa) of the floating-point number.
     *
     * &lt;p&gt;If the argument is positive infinity, the result is
     * {@code 0x7ff0000000000000L}.
     *
     * &lt;p&gt;If the argument is negative infinity, the result is
     * {@code 0xfff0000000000000L}.
     *
     * &lt;p&gt;If the argument is NaN, the result is the {@code long}
     * integer representing the actual NaN value.  Unlike the
     * {@code doubleToLongBits} method,
     * {@code doubleToRawLongBits} does not collapse all the bit
     * patterns encoding a NaN to a single "canonical" NaN
     * value.
     *
     * &lt;p&gt;In all cases, the result is a {@code long} integer that,
     * when given to the {@link #longBitsToDouble(long)} method, will
     * produce a floating-point value the same as the argument to
     * {@code doubleToRawLongBits}.
     *
     * @param   value   a {@code double} precision floating-point number.
     * @return the bits that represent the floating-point number.
     * @since 1.3
     */
    @HotSpotIntrinsicCandidate
    public static native long doubleToRawLongBits(double value);

}

