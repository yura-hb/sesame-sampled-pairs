class GlobalFunctions {
    /**
     * ECMA 15.1.2.3 parseFloat implementation
     *
     * @param self   self reference
     * @param string string to parse
     *
     * @return numeric type representing string contents
     */
    public static double parseFloat(final Object self, final Object string) {
	final String str = JSType.trimLeft(JSType.toString(string));
	final int length = str.length();

	// empty string is not valid
	if (length == 0) {
	    return Double.NaN;
	}

	int start = 0;
	boolean negative = false;
	char ch = str.charAt(0);

	if (ch == '-') {
	    start++;
	    negative = true;
	} else if (ch == '+') {
	    start++;
	} else if (ch == 'N') {
	    if (str.startsWith("NaN")) {
		return Double.NaN;
	    }
	}

	if (start == length) {
	    // just the sign character
	    return Double.NaN;
	}

	ch = str.charAt(start);
	if (ch == 'I') {
	    if (str.substring(start).startsWith("Infinity")) {
		return negative ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
	    }
	}

	boolean dotSeen = false;
	boolean exponentOk = false;
	int exponentOffset = -1;
	int end;

	loop: for (end = start; end &lt; length; end++) {
	    ch = str.charAt(end);

	    switch (ch) {
	    case '.':
		// dot allowed only once
		if (exponentOffset != -1 || dotSeen) {
		    break loop;
		}
		dotSeen = true;
		break;

	    case 'e':
	    case 'E':
		// 'e'/'E' allow only once
		if (exponentOffset != -1) {
		    break loop;
		}
		exponentOffset = end;
		break;

	    case '+':
	    case '-':
		// Sign of the exponent. But allowed only if the
		// previous char in the string was 'e' or 'E'.
		if (exponentOffset != end - 1) {
		    break loop;
		}
		break;

	    case '0':
	    case '1':
	    case '2':
	    case '3':
	    case '4':
	    case '5':
	    case '6':
	    case '7':
	    case '8':
	    case '9':
		if (exponentOffset != -1) {
		    // seeing digit after 'e' or 'E'
		    exponentOk = true;
		}
		break;

	    default: // ignore garbage at the end
		break loop;
	    }
	}

	// ignore 'e'/'E' followed by '+/-' if not real exponent found
	if (exponentOffset != -1 && !exponentOk) {
	    end = exponentOffset;
	}

	if (start == end) {
	    return Double.NaN;
	}

	try {
	    final double result = Double.valueOf(str.substring(start, end));
	    return negative ? -result : result;
	} catch (final NumberFormatException e) {
	    return Double.NaN;
	}
    }

}

