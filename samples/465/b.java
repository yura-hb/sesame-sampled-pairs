abstract class AbstractDateTimeDV extends TypeValidator {
    /**
     * Parses time zone: 'Z' or {+,-} followed by hh:mm
     *
     * @param data
     * @param sign
     * @exception RuntimeException
     */
    protected void getTimeZone(String buffer, DateTimeData data, int sign, int end) throws RuntimeException {
	data.utc = buffer.charAt(sign);

	if (buffer.charAt(sign) == 'Z') {
	    if (end &gt; (++sign)) {
		throw new RuntimeException("Error in parsing time zone");
	    }
	    return;
	}
	if (sign &lt;= (end - 6)) {

	    int negate = buffer.charAt(sign) == '-' ? -1 : 1;
	    //parse hr
	    int stop = ++sign + 2;
	    data.timezoneHr = negate * parseInt(buffer, sign, stop);
	    if (buffer.charAt(stop++) != ':') {
		throw new RuntimeException("Error in parsing time zone");
	    }

	    //parse min
	    data.timezoneMin = negate * parseInt(buffer, stop, stop + 2);

	    if (stop + 2 != end) {
		throw new RuntimeException("Error in parsing time zone");
	    }
	    if (data.timezoneHr != 0 || data.timezoneMin != 0) {
		data.normalized = false;
	    }
	} else {
	    throw new RuntimeException("Error in parsing time zone");
	}
	if (DEBUG) {
	    System.out.println("time[hh]=" + data.timezoneHr + " time[mm]=" + data.timezoneMin);
	}
    }

    private static final boolean DEBUG = false;

    /**
     * Given start and end position, parses string value
     *
     * @param buffer string to parse
     * @param start start position
     * @param end end position
     * @return return integer representation of characters
     */
    protected int parseInt(String buffer, int start, int end) throws NumberFormatException {
	//REVISIT: more testing on this parsing needs to be done.
	int radix = 10;
	int result = 0;
	int digit = 0;
	int limit = -Integer.MAX_VALUE;
	int multmin = limit / radix;
	int i = start;
	do {
	    digit = getDigit(buffer.charAt(i));
	    if (digit &lt; 0) {
		throw new NumberFormatException("'" + buffer + "' has wrong format");
	    }
	    if (result &lt; multmin) {
		throw new NumberFormatException("'" + buffer + "' has wrong format");
	    }
	    result *= radix;
	    if (result &lt; limit + digit) {
		throw new NumberFormatException("'" + buffer + "' has wrong format");
	    }
	    result -= digit;

	} while (++i &lt; end);
	return -result;
    }

}

