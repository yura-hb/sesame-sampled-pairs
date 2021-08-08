import org.apache.commons.lang3.StringUtils;

class DurationFormatUtils {
    /**
     * &lt;p&gt;The internal method to do the formatting.&lt;/p&gt;
     *
     * @param tokens  the tokens
     * @param years  the number of years
     * @param months  the number of months
     * @param days  the number of days
     * @param hours  the number of hours
     * @param minutes  the number of minutes
     * @param seconds  the number of seconds
     * @param milliseconds  the number of millis
     * @param padWithZeros  whether to pad
     * @return the formatted string
     */
    static String format(final Token[] tokens, final long years, final long months, final long days, final long hours,
	    final long minutes, final long seconds, final long milliseconds, final boolean padWithZeros) {
	final StringBuilder buffer = new StringBuilder();
	boolean lastOutputSeconds = false;
	for (final Token token : tokens) {
	    final Object value = token.getValue();
	    final int count = token.getCount();
	    if (value instanceof StringBuilder) {
		buffer.append(value.toString());
	    } else {
		if (value.equals(y)) {
		    buffer.append(paddedValue(years, padWithZeros, count));
		    lastOutputSeconds = false;
		} else if (value.equals(M)) {
		    buffer.append(paddedValue(months, padWithZeros, count));
		    lastOutputSeconds = false;
		} else if (value.equals(d)) {
		    buffer.append(paddedValue(days, padWithZeros, count));
		    lastOutputSeconds = false;
		} else if (value.equals(H)) {
		    buffer.append(paddedValue(hours, padWithZeros, count));
		    lastOutputSeconds = false;
		} else if (value.equals(m)) {
		    buffer.append(paddedValue(minutes, padWithZeros, count));
		    lastOutputSeconds = false;
		} else if (value.equals(s)) {
		    buffer.append(paddedValue(seconds, padWithZeros, count));
		    lastOutputSeconds = true;
		} else if (value.equals(S)) {
		    if (lastOutputSeconds) {
			// ensure at least 3 digits are displayed even if padding is not selected
			final int width = padWithZeros ? Math.max(3, count) : 3;
			buffer.append(paddedValue(milliseconds, true, width));
		    } else {
			buffer.append(paddedValue(milliseconds, padWithZeros, count));
		    }
		    lastOutputSeconds = false;
		}
	    }
	}
	return buffer.toString();
    }

    static final Object y = "y";
    static final Object M = "M";
    static final Object d = "d";
    static final Object H = "H";
    static final Object m = "m";
    static final Object s = "s";
    static final Object S = "S";

    /**
     * &lt;p&gt;Converts a {@code long} to a {@code String} with optional
     * zero padding.&lt;/p&gt;
     *
     * @param value the value to convert
     * @param padWithZeros whether to pad with zeroes
     * @param count the size to pad to (ignored if {@code padWithZeros} is false)
     * @return the string result
     */
    private static String paddedValue(final long value, final boolean padWithZeros, final int count) {
	final String longString = Long.toString(value);
	return padWithZeros ? StringUtils.leftPad(longString, count, '0') : longString;
    }

    class Token {
	static final Object y = "y";
	static final Object M = "M";
	static final Object d = "d";
	static final Object H = "H";
	static final Object m = "m";
	static final Object s = "s";
	static final Object S = "S";

	/**
	 * Gets the particular value this token represents.
	 *
	 * @return Object value
	 */
	Object getValue() {
	    return value;
	}

	/**
	 * Gets the current number of values represented
	 *
	 * @return int number of values represented
	 */
	int getCount() {
	    return count;
	}

    }

}

