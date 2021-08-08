import java.text.NumberFormat;
import java.text.ParsePosition;

class CompositeFormat {
    /**
     * Parses &lt;code&gt;source&lt;/code&gt; for a number.  This method can parse normal,
     * numeric values as well as special values.  These special values include
     * Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY.
     *
     * @param source the string to parse
     * @param format the number format used to parse normal, numeric values.
     * @param pos input/output parsing parameter.
     * @return the parsed number.
     */
    public static Number parseNumber(final String source, final NumberFormat format, final ParsePosition pos) {
	final int startIndex = pos.getIndex();
	Number number = format.parse(source, pos);
	final int endIndex = pos.getIndex();

	// check for error parsing number
	if (startIndex == endIndex) {
	    // try parsing special numbers
	    final double[] special = { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY };
	    for (int i = 0; i &lt; special.length; ++i) {
		number = parseNumber(source, special[i], pos);
		if (number != null) {
		    break;
		}
	    }
	}

	return number;
    }

    /**
     * Parses &lt;code&gt;source&lt;/code&gt; for special double values.  These values
     * include Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY.
     *
     * @param source the string to parse
     * @param value the special value to parse.
     * @param pos input/output parsing parameter.
     * @return the special number.
     */
    private static Number parseNumber(final String source, final double value, final ParsePosition pos) {
	Number ret = null;

	StringBuilder sb = new StringBuilder();
	sb.append('(');
	sb.append(value);
	sb.append(')');

	final int n = sb.length();
	final int startIndex = pos.getIndex();
	final int endIndex = startIndex + n;
	if (endIndex &lt; source.length() && source.substring(startIndex, endIndex).compareTo(sb.toString()) == 0) {
	    ret = Double.valueOf(value);
	    pos.setIndex(endIndex);
	}

	return ret;
    }

}

