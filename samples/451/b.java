import static com.google.common.math.MathPreconditions.checkNoOverflow;

class LongMath {
    /**
    * Returns the product of {@code a} and {@code b}, provided it does not overflow.
    *
    * @throws ArithmeticException if {@code a * b} overflows in signed {@code long} arithmetic
    */
    public static long checkedMultiply(long a, long b) {
	// Hacker's Delight, Section 2-12
	int leadingZeros = Long.numberOfLeadingZeros(a) + Long.numberOfLeadingZeros(~a) + Long.numberOfLeadingZeros(b)
		+ Long.numberOfLeadingZeros(~b);
	/*
	 * If leadingZeros &gt; Long.SIZE + 1 it's definitely fine, if it's &lt; Long.SIZE it's definitely
	 * bad. We do the leadingZeros check to avoid the division below if at all possible.
	 *
	 * Otherwise, if b == Long.MIN_VALUE, then the only allowed values of a are 0 and 1. We take
	 * care of all a &lt; 0 with their own check, because in particular, the case a == -1 will
	 * incorrectly pass the division check below.
	 *
	 * In all other cases, we check that either a is 0 or the result is consistent with division.
	 */
	if (leadingZeros &gt; Long.SIZE + 1) {
	    return a * b;
	}
	checkNoOverflow(leadingZeros &gt;= Long.SIZE, "checkedMultiply", a, b);
	checkNoOverflow(a &gt;= 0 | b != Long.MIN_VALUE, "checkedMultiply", a, b);
	long result = a * b;
	checkNoOverflow(a == 0 || result / a == b, "checkedMultiply", a, b);
	return result;
    }

}

