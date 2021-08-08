import java.util.regex.Pattern;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

class CheckUtil {
    /**
     * Returns the value represented by the specified string of the specified
     * type. Returns 0 for types other than float, double, int, and long.
     * @param text the string to be parsed.
     * @param type the token type of the text. Should be a constant of
     *     {@link TokenTypes}.
     * @return the double value represented by the string argument.
     */
    public static double parseDouble(String text, int type) {
	String txt = UNDERSCORE_PATTERN.matcher(text).replaceAll("");
	final double result;
	switch (type) {
	case TokenTypes.NUM_FLOAT:
	case TokenTypes.NUM_DOUBLE:
	    result = Double.parseDouble(txt);
	    break;
	case TokenTypes.NUM_INT:
	case TokenTypes.NUM_LONG:
	    int radix = BASE_10;
	    if (txt.startsWith("0x") || txt.startsWith("0X")) {
		radix = BASE_16;
		txt = txt.substring(2);
	    } else if (txt.startsWith("0b") || txt.startsWith("0B")) {
		radix = BASE_2;
		txt = txt.substring(2);
	    } else if (CommonUtil.startsWithChar(txt, '0')) {
		radix = BASE_8;
		txt = txt.substring(1);
	    }
	    result = parseNumber(txt, radix, type);
	    break;
	default:
	    result = Double.NaN;
	    break;
	}
	return result;
    }

    /** Pattern matching underscore characters ('_'). */
    private static final Pattern UNDERSCORE_PATTERN = Pattern.compile("_");
    /** Decimal radix. */
    private static final int BASE_10 = 10;
    /** Hex radix. */
    private static final int BASE_16 = 16;
    /** Binary radix. */
    private static final int BASE_2 = 2;
    /** Octal radix. */
    private static final int BASE_8 = 8;

    /**
     * Parses the string argument as an integer or a long in the radix specified by
     * the second argument. The characters in the string must all be digits of
     * the specified radix.
     * @param text the String containing the integer representation to be
     *     parsed. Precondition: text contains a parsable int.
     * @param radix the radix to be used while parsing text.
     * @param type the token type of the text. Should be a constant of
     *     {@link TokenTypes}.
     * @return the number represented by the string argument in the specified radix.
     */
    private static double parseNumber(final String text, final int radix, final int type) {
	String txt = text;
	if (CommonUtil.endsWithChar(txt, 'L') || CommonUtil.endsWithChar(txt, 'l')) {
	    txt = txt.substring(0, txt.length() - 1);
	}
	final double result;
	if (txt.isEmpty()) {
	    result = 0.0;
	} else {
	    final boolean negative = txt.charAt(0) == '-';
	    if (type == TokenTypes.NUM_INT) {
		if (negative) {
		    result = Integer.parseInt(txt, radix);
		} else {
		    result = Integer.parseUnsignedInt(txt, radix);
		}
	    } else {
		if (negative) {
		    result = Long.parseLong(txt, radix);
		} else {
		    result = Long.parseUnsignedLong(txt, radix);
		}
	    }
	}
	return result;
    }

}

