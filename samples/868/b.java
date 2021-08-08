import java.util.Objects;

class Long extends Number implements Comparable&lt;Long&gt; {
    /**
     * Parses the {@link CharSequence} argument as an unsigned {@code long} in
     * the specified {@code radix}, beginning at the specified
     * {@code beginIndex} and extending to {@code endIndex - 1}.
     *
     * &lt;p&gt;The method does not take steps to guard against the
     * {@code CharSequence} being mutated while parsing.
     *
     * @param      s   the {@code CharSequence} containing the unsigned
     *                 {@code long} representation to be parsed
     * @param      beginIndex   the beginning index, inclusive.
     * @param      endIndex     the ending index, exclusive.
     * @param      radix   the radix to be used while parsing {@code s}.
     * @return     the unsigned {@code long} represented by the subsequence in
     *             the specified radix.
     * @throws     NullPointerException  if {@code s} is null.
     * @throws     IndexOutOfBoundsException  if {@code beginIndex} is
     *             negative, or if {@code beginIndex} is greater than
     *             {@code endIndex} or if {@code endIndex} is greater than
     *             {@code s.length()}.
     * @throws     NumberFormatException  if the {@code CharSequence} does not
     *             contain a parsable unsigned {@code long} in the specified
     *             {@code radix}, or if {@code radix} is either smaller than
     *             {@link java.lang.Character#MIN_RADIX} or larger than
     *             {@link java.lang.Character#MAX_RADIX}.
     * @since  9
     */
    public static long parseUnsignedLong(CharSequence s, int beginIndex, int endIndex, int radix)
	    throws NumberFormatException {
	s = Objects.requireNonNull(s);

	if (beginIndex &lt; 0 || beginIndex &gt; endIndex || endIndex &gt; s.length()) {
	    throw new IndexOutOfBoundsException();
	}
	int start = beginIndex, len = endIndex - beginIndex;

	if (len &gt; 0) {
	    char firstChar = s.charAt(start);
	    if (firstChar == '-') {
		throw new NumberFormatException(String.format("Illegal leading minus sign " + "on unsigned string %s.",
			s.subSequence(start, start + len)));
	    } else {
		if (len &lt;= 12 || // Long.MAX_VALUE in Character.MAX_RADIX is 13 digits
			(radix == 10 && len &lt;= 18)) { // Long.MAX_VALUE in base 10 is 19 digits
		    return parseLong(s, start, start + len, radix);
		}

		// No need for range checks on end due to testing above.
		long first = parseLong(s, start, start + len - 1, radix);
		int second = Character.digit(s.charAt(start + len - 1), radix);
		if (second &lt; 0) {
		    throw new NumberFormatException("Bad digit at end of " + s.subSequence(start, start + len));
		}
		long result = first * radix + second;

		/*
		 * Test leftmost bits of multiprecision extension of first*radix
		 * for overflow. The number of bits needed is defined by
		 * GUARD_BIT = ceil(log2(Character.MAX_RADIX)) + 1 = 7. Then
		 * int guard = radix*(int)(first &gt;&gt;&gt; (64 - GUARD_BIT)) and
		 * overflow is tested by splitting guard in the ranges
		 * guard &lt; 92, 92 &lt;= guard &lt; 128, and 128 &lt;= guard, where
		 * 92 = 128 - Character.MAX_RADIX. Note that guard cannot take
		 * on a value which does not include a prime factor in the legal
		 * radix range.
		 */
		int guard = radix * (int) (first &gt;&gt;&gt; 57);
		if (guard &gt;= 128 || (result &gt;= 0 && guard &gt;= 128 - Character.MAX_RADIX)) {
		    /*
		     * For purposes of exposition, the programmatic statements
		     * below should be taken to be multi-precision, i.e., not
		     * subject to overflow.
		     *
		     * A) Condition guard &gt;= 128:
		     * If guard &gt;= 128 then first*radix &gt;= 2^7 * 2^57 = 2^64
		     * hence always overflow.
		     *
		     * B) Condition guard &lt; 92:
		     * Define left7 = first &gt;&gt;&gt; 57.
		     * Given first = (left7 * 2^57) + (first & (2^57 - 1)) then
		     * result &lt;= (radix*left7)*2^57 + radix*(2^57 - 1) + second.
		     * Thus if radix*left7 &lt; 92, radix &lt;= 36, and second &lt; 36,
		     * then result &lt; 92*2^57 + 36*(2^57 - 1) + 36 = 2^64 hence
		     * never overflow.
		     *
		     * C) Condition 92 &lt;= guard &lt; 128:
		     * first*radix + second &gt;= radix*left7*2^57 + second
		     * so that first*radix + second &gt;= 92*2^57 + 0 &gt; 2^63
		     *
		     * D) Condition guard &lt; 128:
		     * radix*first &lt;= (radix*left7) * 2^57 + radix*(2^57 - 1)
		     * so
		     * radix*first + second &lt;= (radix*left7) * 2^57 + radix*(2^57 - 1) + 36
		     * thus
		     * radix*first + second &lt; 128 * 2^57 + 36*2^57 - radix + 36
		     * whence
		     * radix*first + second &lt; 2^64 + 2^6*2^57 = 2^64 + 2^63
		     *
		     * E) Conditions C, D, and result &gt;= 0:
		     * C and D combined imply the mathematical result
		     * 2^63 &lt; first*radix + second &lt; 2^64 + 2^63. The lower
		     * bound is therefore negative as a signed long, but the
		     * upper bound is too small to overflow again after the
		     * signed long overflows to positive above 2^64 - 1. Hence
		     * result &gt;= 0 implies overflow given C and D.
		     */
		    throw new NumberFormatException(String.format(
			    "String value %s exceeds " + "range of unsigned long.", s.subSequence(start, start + len)));
		}
		return result;
	    }
	} else {
	    throw NumberFormatException.forInputString("");
	}
    }

    /**
     * A constant holding the maximum value a {@code long} can
     * have, 2&lt;sup&gt;63&lt;/sup&gt;-1.
     */
    @Native
    public static final long MAX_VALUE = 0x7fffffffffffffffL;
    /**
     * A constant holding the minimum value a {@code long} can
     * have, -2&lt;sup&gt;63&lt;/sup&gt;.
     */
    @Native
    public static final long MIN_VALUE = 0x8000000000000000L;

    /**
     * Parses the {@link CharSequence} argument as a signed {@code long} in
     * the specified {@code radix}, beginning at the specified
     * {@code beginIndex} and extending to {@code endIndex - 1}.
     *
     * &lt;p&gt;The method does not take steps to guard against the
     * {@code CharSequence} being mutated while parsing.
     *
     * @param      s   the {@code CharSequence} containing the {@code long}
     *                  representation to be parsed
     * @param      beginIndex   the beginning index, inclusive.
     * @param      endIndex     the ending index, exclusive.
     * @param      radix   the radix to be used while parsing {@code s}.
     * @return     the signed {@code long} represented by the subsequence in
     *             the specified radix.
     * @throws     NullPointerException  if {@code s} is null.
     * @throws     IndexOutOfBoundsException  if {@code beginIndex} is
     *             negative, or if {@code beginIndex} is greater than
     *             {@code endIndex} or if {@code endIndex} is greater than
     *             {@code s.length()}.
     * @throws     NumberFormatException  if the {@code CharSequence} does not
     *             contain a parsable {@code int} in the specified
     *             {@code radix}, or if {@code radix} is either smaller than
     *             {@link java.lang.Character#MIN_RADIX} or larger than
     *             {@link java.lang.Character#MAX_RADIX}.
     * @since  9
     */
    public static long parseLong(CharSequence s, int beginIndex, int endIndex, int radix) throws NumberFormatException {
	s = Objects.requireNonNull(s);

	if (beginIndex &lt; 0 || beginIndex &gt; endIndex || endIndex &gt; s.length()) {
	    throw new IndexOutOfBoundsException();
	}
	if (radix &lt; Character.MIN_RADIX) {
	    throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
	}
	if (radix &gt; Character.MAX_RADIX) {
	    throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
	}

	boolean negative = false;
	int i = beginIndex;
	long limit = -Long.MAX_VALUE;

	if (i &lt; endIndex) {
	    char firstChar = s.charAt(i);
	    if (firstChar &lt; '0') { // Possible leading "+" or "-"
		if (firstChar == '-') {
		    negative = true;
		    limit = Long.MIN_VALUE;
		} else if (firstChar != '+') {
		    throw NumberFormatException.forCharSequence(s, beginIndex, endIndex, i);
		}
		i++;
	    }
	    if (i &gt;= endIndex) { // Cannot have lone "+", "-" or ""
		throw NumberFormatException.forCharSequence(s, beginIndex, endIndex, i);
	    }
	    long multmin = limit / radix;
	    long result = 0;
	    while (i &lt; endIndex) {
		// Accumulating negatively avoids surprises near MAX_VALUE
		int digit = Character.digit(s.charAt(i), radix);
		if (digit &lt; 0 || result &lt; multmin) {
		    throw NumberFormatException.forCharSequence(s, beginIndex, endIndex, i);
		}
		result *= radix;
		if (result &lt; limit + digit) {
		    throw NumberFormatException.forCharSequence(s, beginIndex, endIndex, i);
		}
		i++;
		result -= digit;
	    }
	    return negative ? result : -result;
	} else {
	    throw new NumberFormatException("");
	}
    }

}

