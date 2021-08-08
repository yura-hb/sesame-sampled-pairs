class DoubleConversion {
    /**
     * Converts a double number to a string representation with a fixed number of digits
     * after the decimal point.
     *
     * @param value number to convert.
     * @param requestedDigits number of digits after decimal point
     * @return formatted number
     */
    public static String toFixed(final double value, final int requestedDigits) {
	final DtoaBuffer buffer = new DtoaBuffer(BUFFER_LENGTH);
	final double absValue = Math.abs(value);

	if (value &lt; 0) {
	    buffer.isNegative = true;
	}

	if (value == 0) {
	    buffer.append('0');
	    buffer.decimalPoint = 1;
	} else if (!fixedDtoa(absValue, requestedDigits, buffer)) {
	    buffer.reset();
	    bignumDtoa(absValue, DtoaMode.FIXED, requestedDigits, buffer);
	}

	return buffer.format(DtoaMode.FIXED, requestedDigits);
    }

    private final static int BUFFER_LENGTH = 30;

    /**
     * Converts a double number to a string representation with a
     * fixed number of digits after the decimal point using the
     * {@code FixedDtoa} algorithm.
     *
     * @param v number to convert.
     * @param digits number of digits after the decimal point
     * @param buffer buffer to use
     * @return true if conversion succeeded
     */
    public static boolean fixedDtoa(final double v, final int digits, final DtoaBuffer buffer) {
	assert (v &gt; 0);
	assert (!Double.isNaN(v));
	assert (!Double.isInfinite(v));

	return FixedDtoa.fastFixedDtoa(v, digits, buffer);
    }

    /**
     * Converts a double number to a string representation using the
     * {@code BignumDtoa} algorithm and the specified conversion mode
     * and number of digits.
     *
     * @param v number to convert
     * @param mode conversion mode
     * @param digits number of digits
     * @param buffer buffer to use
     */
    public static void bignumDtoa(final double v, final DtoaMode mode, final int digits, final DtoaBuffer buffer) {
	assert (v &gt; 0);
	assert (!Double.isNaN(v));
	assert (!Double.isInfinite(v));

	BignumDtoa.bignumDtoa(v, mode, digits, buffer);
    }

}

