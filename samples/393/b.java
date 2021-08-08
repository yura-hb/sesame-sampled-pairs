abstract class NumberFormat extends Format {
    /**
     * Specialization of format.
     *
     * @param number the double number to format
     * @return the formatted String
     * @exception        ArithmeticException if rounding is needed with rounding
     *                   mode being set to RoundingMode.UNNECESSARY
     * @see java.text.Format#format
     */
    public final String format(double number) {
	// Use fast-path for double result if that works
	String result = fastFormat(number);
	if (result != null)
	    return result;

	return format(number, new StringBuffer(), DontCareFieldPosition.INSTANCE).toString();
    }

    String fastFormat(double number) {
	return null;
    }

    /**
     * Specialization of format.
     *
     * @param number     the double number to format
     * @param toAppendTo the StringBuffer to which the formatted text is to be
     *                   appended
     * @param pos        keeps track on the position of the field within the
     *                   returned string. For example, for formatting a number
     *                   {@code 1234567.89} in {@code Locale.US} locale,
     *                   if the given {@code fieldPosition} is
     *                   {@link NumberFormat#INTEGER_FIELD}, the begin index
     *                   and end index of {@code fieldPosition} will be set
     *                   to 0 and 9, respectively for the output string
     *                   {@code 1,234,567.89}.
     * @return the formatted StringBuffer
     * @exception        ArithmeticException if rounding is needed with rounding
     *                   mode being set to RoundingMode.UNNECESSARY
     * @see java.text.Format#format
     */
    public abstract StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos);

}

