import java.util.Arrays;

class Dfp implements RealFieldElement&lt;Dfp&gt; {
    /** Compute the square root.
     * @return square root of the instance
     * @since 3.2
     */
    @Override
    public Dfp sqrt() {

	// check for unusual cases
	if (nans == FINITE && mant[mant.length - 1] == 0) {
	    // if zero
	    return newInstance(this);
	}

	if (nans != FINITE) {
	    if (nans == INFINITE && sign == 1) {
		// if positive infinity
		return newInstance(this);
	    }

	    if (nans == QNAN) {
		return newInstance(this);
	    }

	    if (nans == SNAN) {
		Dfp result;

		field.setIEEEFlagsBits(DfpField.FLAG_INVALID);
		result = newInstance(this);
		result = dotrap(DfpField.FLAG_INVALID, SQRT_TRAP, null, result);
		return result;
	    }
	}

	if (sign == -1) {
	    // if negative
	    Dfp result;

	    field.setIEEEFlagsBits(DfpField.FLAG_INVALID);
	    result = newInstance(this);
	    result.nans = QNAN;
	    result = dotrap(DfpField.FLAG_INVALID, SQRT_TRAP, null, result);
	    return result;
	}

	Dfp x = newInstance(this);

	/* Lets make a reasonable guess as to the size of the square root */
	if (x.exp &lt; -1 || x.exp &gt; 1) {
	    x.exp = this.exp / 2;
	}

	/* Coarsely estimate the mantissa */
	switch (x.mant[mant.length - 1] / 2000) {
	case 0:
	    x.mant[mant.length - 1] = x.mant[mant.length - 1] / 2 + 1;
	    break;
	case 2:
	    x.mant[mant.length - 1] = 1500;
	    break;
	case 3:
	    x.mant[mant.length - 1] = 2200;
	    break;
	default:
	    x.mant[mant.length - 1] = 3000;
	}

	Dfp dx = newInstance(x);

	/* Now that we have the first pass estimate, compute the rest
	by the formula dx = (y - x*x) / (2x); */

	Dfp px = getZero();
	Dfp ppx = getZero();
	while (x.unequal(px)) {
	    dx = newInstance(x);
	    dx.sign = -1;
	    dx = dx.add(this.divide(x));
	    dx = dx.divide(2);
	    ppx = px;
	    px = x;
	    x = x.add(dx);

	    if (x.equals(ppx)) {
		// alternating between two values
		break;
	    }

	    // if dx is zero, break.  Note testing the most sig digit
	    // is a sufficient test since dx is normalized
	    if (dx.mant[mant.length - 1] == 0) {
		break;
	    }
	}

	return x;

    }

    /** Indicator for non-finite / non-number values. */
    protected byte nans;
    /** Indicator value for normal finite numbers. */
    public static final byte FINITE = 0;
    /** Mantissa. */
    protected int[] mant;
    /** Indicator value for Infinity. */
    public static final byte INFINITE = 1;
    /** Sign bit: 1 for positive, -1 for negative. */
    protected byte sign;
    /** Indicator value for quiet NaN. */
    public static final byte QNAN = 3;
    /** Indicator value for signaling NaN. */
    public static final byte SNAN = 2;
    /** Factory building similar Dfp's. */
    private final DfpField field;
    /** Name for traps triggered by square root. */
    private static final String SQRT_TRAP = "sqrt";
    /** Exponent. */
    protected int exp;
    /** Name for traps triggered by newInstance. */
    private static final String NEW_INSTANCE_TRAP = "newInstance";
    /** The minimum exponent before underflow is signaled.  Flush to zero
     *  occurs at minExp-DIGITS */
    public static final int MIN_EXP = -32767;
    /** The amount under/overflows are scaled by before going to trap handler */
    public static final int ERR_SCALE = 32760;
    /** Name for traps triggered by division. */
    private static final String DIVIDE_TRAP = "divide";
    /** The radix, or base of this system.  Set to 10000 */
    public static final int RADIX = 10000;
    /** Name for traps triggered by addition. */
    private static final String ADD_TRAP = "add";
    /** Name for traps triggered by greaterThan. */
    private static final String GREATER_THAN_TRAP = "greaterThan";
    /** Name for traps triggered by lessThan. */
    private static final String LESS_THAN_TRAP = "lessThan";
    /** The maximum exponent before overflow is signaled and results flushed
     *  to infinity */
    public static final int MAX_EXP = 32768;
    /** Name for traps triggered by alignment. */
    private static final String ALIGN_TRAP = "align";

    /** Create an instance by copying an existing one.
     * Use this internally in preference to constructors to facilitate subclasses.
     * @param d instance to copy
     * @return a new instance with the same value as d
     */
    public Dfp newInstance(final Dfp d) {

	// make sure we don't mix number with different precision
	if (field.getRadixDigits() != d.field.getRadixDigits()) {
	    field.setIEEEFlagsBits(DfpField.FLAG_INVALID);
	    final Dfp result = newInstance(getZero());
	    result.nans = QNAN;
	    return dotrap(DfpField.FLAG_INVALID, NEW_INSTANCE_TRAP, d, result);
	}

	return new Dfp(d);

    }

    /** Raises a trap.  This does not set the corresponding flag however.
     *  @param type the trap type
     *  @param what - name of routine trap occurred in
     *  @param oper - input operator to function
     *  @param result - the result computed prior to the trap
     *  @return The suggested return value from the trap handler
     */
    public Dfp dotrap(int type, String what, Dfp oper, Dfp result) {
	Dfp def = result;

	switch (type) {
	case DfpField.FLAG_INVALID:
	    def = newInstance(getZero());
	    def.sign = result.sign;
	    def.nans = QNAN;
	    break;

	case DfpField.FLAG_DIV_ZERO:
	    if (nans == FINITE && mant[mant.length - 1] != 0) {
		// normal case, we are finite, non-zero
		def = newInstance(getZero());
		def.sign = (byte) (sign * oper.sign);
		def.nans = INFINITE;
	    }

	    if (nans == FINITE && mant[mant.length - 1] == 0) {
		//  0/0
		def = newInstance(getZero());
		def.nans = QNAN;
	    }

	    if (nans == INFINITE || nans == QNAN) {
		def = newInstance(getZero());
		def.nans = QNAN;
	    }

	    if (nans == INFINITE || nans == SNAN) {
		def = newInstance(getZero());
		def.nans = QNAN;
	    }
	    break;

	case DfpField.FLAG_UNDERFLOW:
	    if ((result.exp + mant.length) &lt; MIN_EXP) {
		def = newInstance(getZero());
		def.sign = result.sign;
	    } else {
		def = newInstance(result); // gradual underflow
	    }
	    result.exp += ERR_SCALE;
	    break;

	case DfpField.FLAG_OVERFLOW:
	    result.exp -= ERR_SCALE;
	    def = newInstance(getZero());
	    def.sign = result.sign;
	    def.nans = INFINITE;
	    break;

	default:
	    def = result;
	    break;
	}

	return trap(type, what, oper, def, result);

    }

    /** Get the constant 0.
     * @return a Dfp with value zero
     */
    public Dfp getZero() {
	return field.getZero();
    }

    /** Check if instance is not equal to x.
     * @param x number to check instance against
     * @return true if instance is not equal to x and neither are NaN, false otherwise
     */
    public boolean unequal(final Dfp x) {
	if (isNaN() || x.isNaN() || field.getRadixDigits() != x.field.getRadixDigits()) {
	    return false;
	}

	return greaterThan(x) || lessThan(x);
    }

    /** Divide this by divisor.
     * @param divisor divisor
     * @return quotient of this by divisor
     */
    @Override
    public Dfp divide(Dfp divisor) {
	int dividend[]; // current status of the dividend
	int quotient[]; // quotient
	int remainder[];// remainder
	int qd; // current quotient digit we're working with
	int nsqd; // number of significant quotient digits we have
	int trial = 0; // trial quotient digit
	int minadj; // minimum adjustment
	boolean trialgood; // Flag to indicate a good trail digit
	int md = 0; // most sig digit in result
	int excp; // exceptions

	// make sure we don't mix number with different precision
	if (field.getRadixDigits() != divisor.field.getRadixDigits()) {
	    field.setIEEEFlagsBits(DfpField.FLAG_INVALID);
	    final Dfp result = newInstance(getZero());
	    result.nans = QNAN;
	    return dotrap(DfpField.FLAG_INVALID, DIVIDE_TRAP, divisor, result);
	}

	Dfp result = newInstance(getZero());

	/* handle special cases */
	if (nans != FINITE || divisor.nans != FINITE) {
	    if (isNaN()) {
		return this;
	    }

	    if (divisor.isNaN()) {
		return divisor;
	    }

	    if (nans == INFINITE && divisor.nans == FINITE) {
		result = newInstance(this);
		result.sign = (byte) (sign * divisor.sign);
		return result;
	    }

	    if (divisor.nans == INFINITE && nans == FINITE) {
		result = newInstance(getZero());
		result.sign = (byte) (sign * divisor.sign);
		return result;
	    }

	    if (divisor.nans == INFINITE && nans == INFINITE) {
		field.setIEEEFlagsBits(DfpField.FLAG_INVALID);
		result = newInstance(getZero());
		result.nans = QNAN;
		result = dotrap(DfpField.FLAG_INVALID, DIVIDE_TRAP, divisor, result);
		return result;
	    }
	}

	/* Test for divide by zero */
	if (divisor.mant[mant.length - 1] == 0) {
	    field.setIEEEFlagsBits(DfpField.FLAG_DIV_ZERO);
	    result = newInstance(getZero());
	    result.sign = (byte) (sign * divisor.sign);
	    result.nans = INFINITE;
	    result = dotrap(DfpField.FLAG_DIV_ZERO, DIVIDE_TRAP, divisor, result);
	    return result;
	}

	dividend = new int[mant.length + 1]; // one extra digit needed
	quotient = new int[mant.length + 2]; // two extra digits needed 1 for overflow, 1 for rounding
	remainder = new int[mant.length + 1]; // one extra digit needed

	/* Initialize our most significant digits to zero */

	dividend[mant.length] = 0;
	quotient[mant.length] = 0;
	quotient[mant.length + 1] = 0;
	remainder[mant.length] = 0;

	/* copy our mantissa into the dividend, initialize the
	quotient while we are at it */

	for (int i = 0; i &lt; mant.length; i++) {
	    dividend[i] = mant[i];
	    quotient[i] = 0;
	    remainder[i] = 0;
	}

	/* outer loop.  Once per quotient digit */
	nsqd = 0;
	for (qd = mant.length + 1; qd &gt;= 0; qd--) {
	    /* Determine outer limits of our quotient digit */

	    // r =  most sig 2 digits of dividend
	    final int divMsb = dividend[mant.length] * RADIX + dividend[mant.length - 1];
	    int min = divMsb / (divisor.mant[mant.length - 1] + 1);
	    int max = (divMsb + 1) / divisor.mant[mant.length - 1];

	    trialgood = false;
	    while (!trialgood) {
		// try the mean
		trial = (min + max) / 2;

		/* Multiply by divisor and store as remainder */
		int rh = 0;
		for (int i = 0; i &lt; mant.length + 1; i++) {
		    int dm = (i &lt; mant.length) ? divisor.mant[i] : 0;
		    final int r = (dm * trial) + rh;
		    rh = r / RADIX;
		    remainder[i] = r - rh * RADIX;
		}

		/* subtract the remainder from the dividend */
		rh = 1; // carry in to aid the subtraction
		for (int i = 0; i &lt; mant.length + 1; i++) {
		    final int r = ((RADIX - 1) - remainder[i]) + dividend[i] + rh;
		    rh = r / RADIX;
		    remainder[i] = r - rh * RADIX;
		}

		/* Lets analyze what we have here */
		if (rh == 0) {
		    // trial is too big -- negative remainder
		    max = trial - 1;
		    continue;
		}

		/* find out how far off the remainder is telling us we are */
		minadj = (remainder[mant.length] * RADIX) + remainder[mant.length - 1];
		minadj /= divisor.mant[mant.length - 1] + 1;

		if (minadj &gt;= 2) {
		    min = trial + minadj; // update the minimum
		    continue;
		}

		/* May have a good one here, check more thoroughly.  Basically
		its a good one if it is less than the divisor */
		trialgood = false; // assume false
		for (int i = mant.length - 1; i &gt;= 0; i--) {
		    if (divisor.mant[i] &gt; remainder[i]) {
			trialgood = true;
		    }
		    if (divisor.mant[i] &lt; remainder[i]) {
			break;
		    }
		}

		if (remainder[mant.length] != 0) {
		    trialgood = false;
		}

		if (trialgood == false) {
		    min = trial + 1;
		}
	    }

	    /* Great we have a digit! */
	    quotient[qd] = trial;
	    if (trial != 0 || nsqd != 0) {
		nsqd++;
	    }

	    if (field.getRoundingMode() == DfpField.RoundingMode.ROUND_DOWN && nsqd == mant.length) {
		// We have enough for this mode
		break;
	    }

	    if (nsqd &gt; mant.length) {
		// We have enough digits
		break;
	    }

	    /* move the remainder into the dividend while left shifting */
	    dividend[0] = 0;
	    for (int i = 0; i &lt; mant.length; i++) {
		dividend[i + 1] = remainder[i];
	    }
	}

	/* Find the most sig digit */
	md = mant.length; // default
	for (int i = mant.length + 1; i &gt;= 0; i--) {
	    if (quotient[i] != 0) {
		md = i;
		break;
	    }
	}

	/* Copy the digits into the result */
	for (int i = 0; i &lt; mant.length; i++) {
	    result.mant[mant.length - i - 1] = quotient[md - i];
	}

	/* Fixup the exponent. */
	result.exp = exp - divisor.exp + md - mant.length;
	result.sign = (byte) ((sign == divisor.sign) ? 1 : -1);

	if (result.mant[mant.length - 1] == 0) { // if result is zero, set exp to zero
	    result.exp = 0;
	}

	if (md &gt; (mant.length - 1)) {
	    excp = result.round(quotient[md - mant.length]);
	} else {
	    excp = result.round(0);
	}

	if (excp != 0) {
	    result = dotrap(excp, DIVIDE_TRAP, divisor, result);
	}

	return result;
    }

    /** Add x to this.
     * @param x number to add
     * @return sum of this and x
     */
    @Override
    public Dfp add(final Dfp x) {

	// make sure we don't mix number with different precision
	if (field.getRadixDigits() != x.field.getRadixDigits()) {
	    field.setIEEEFlagsBits(DfpField.FLAG_INVALID);
	    final Dfp result = newInstance(getZero());
	    result.nans = QNAN;
	    return dotrap(DfpField.FLAG_INVALID, ADD_TRAP, x, result);
	}

	/* handle special cases */
	if (nans != FINITE || x.nans != FINITE) {
	    if (isNaN()) {
		return this;
	    }

	    if (x.isNaN()) {
		return x;
	    }

	    if (nans == INFINITE && x.nans == FINITE) {
		return this;
	    }

	    if (x.nans == INFINITE && nans == FINITE) {
		return x;
	    }

	    if (x.nans == INFINITE && nans == INFINITE && sign == x.sign) {
		return x;
	    }

	    if (x.nans == INFINITE && nans == INFINITE && sign != x.sign) {
		field.setIEEEFlagsBits(DfpField.FLAG_INVALID);
		Dfp result = newInstance(getZero());
		result.nans = QNAN;
		result = dotrap(DfpField.FLAG_INVALID, ADD_TRAP, x, result);
		return result;
	    }
	}

	/* copy this and the arg */
	Dfp a = newInstance(this);
	Dfp b = newInstance(x);

	/* initialize the result object */
	Dfp result = newInstance(getZero());

	/* Make all numbers positive, but remember their sign */
	final byte asign = a.sign;
	final byte bsign = b.sign;

	a.sign = 1;
	b.sign = 1;

	/* The result will be signed like the arg with greatest magnitude */
	byte rsign = bsign;
	if (compare(a, b) &gt; 0) {
	    rsign = asign;
	}

	/* Handle special case when a or b is zero, by setting the exponent
	of the zero number equal to the other one.  This avoids an alignment
	which would cause catastropic loss of precision */
	if (b.mant[mant.length - 1] == 0) {
	    b.exp = a.exp;
	}

	if (a.mant[mant.length - 1] == 0) {
	    a.exp = b.exp;
	}

	/* align number with the smaller exponent */
	int aextradigit = 0;
	int bextradigit = 0;
	if (a.exp &lt; b.exp) {
	    aextradigit = a.align(b.exp);
	} else {
	    bextradigit = b.align(a.exp);
	}

	/* complement the smaller of the two if the signs are different */
	if (asign != bsign) {
	    if (asign == rsign) {
		bextradigit = b.complement(bextradigit);
	    } else {
		aextradigit = a.complement(aextradigit);
	    }
	}

	/* add the mantissas */
	int rh = 0; /* acts as a carry */
	for (int i = 0; i &lt; mant.length; i++) {
	    final int r = a.mant[i] + b.mant[i] + rh;
	    rh = r / RADIX;
	    result.mant[i] = r - rh * RADIX;
	}
	result.exp = a.exp;
	result.sign = rsign;

	/* handle overflow -- note, when asign!=bsign an overflow is
	 * normal and should be ignored.  */

	if (rh != 0 && (asign == bsign)) {
	    final int lostdigit = result.mant[0];
	    result.shiftRight();
	    result.mant[mant.length - 1] = rh;
	    final int excp = result.round(lostdigit);
	    if (excp != 0) {
		result = dotrap(excp, ADD_TRAP, x, result);
	    }
	}

	/* normalize the result */
	for (int i = 0; i &lt; mant.length; i++) {
	    if (result.mant[mant.length - 1] != 0) {
		break;
	    }
	    result.shiftLeft();
	    if (i == 0) {
		result.mant[0] = aextradigit + bextradigit;
		aextradigit = 0;
		bextradigit = 0;
	    }
	}

	/* result is zero if after normalization the most sig. digit is zero */
	if (result.mant[mant.length - 1] == 0) {
	    result.exp = 0;

	    if (asign != bsign) {
		// Unless adding 2 negative zeros, sign is positive
		result.sign = 1; // Per IEEE 854-1987 Section 6.3
	    }
	}

	/* Call round to test for over/under flows */
	final int excp = result.round(aextradigit + bextradigit);
	if (excp != 0) {
	    result = dotrap(excp, ADD_TRAP, x, result);
	}

	return result;
    }

    /** Divide by a single digit less than radix.
     *  Special case, so there are speed advantages. 0 &lt;= divisor &lt; radix
     * @param divisor divisor
     * @return quotient of this by divisor
     */
    public Dfp divide(int divisor) {

	// Handle special cases
	if (nans != FINITE) {
	    if (isNaN()) {
		return this;
	    }

	    if (nans == INFINITE) {
		return newInstance(this);
	    }
	}

	// Test for divide by zero
	if (divisor == 0) {
	    field.setIEEEFlagsBits(DfpField.FLAG_DIV_ZERO);
	    Dfp result = newInstance(getZero());
	    result.sign = sign;
	    result.nans = INFINITE;
	    result = dotrap(DfpField.FLAG_DIV_ZERO, DIVIDE_TRAP, getZero(), result);
	    return result;
	}

	// range check divisor
	if (divisor &lt; 0 || divisor &gt;= RADIX) {
	    field.setIEEEFlagsBits(DfpField.FLAG_INVALID);
	    Dfp result = newInstance(getZero());
	    result.nans = QNAN;
	    result = dotrap(DfpField.FLAG_INVALID, DIVIDE_TRAP, result, result);
	    return result;
	}

	Dfp result = newInstance(this);

	int rl = 0;
	for (int i = mant.length - 1; i &gt;= 0; i--) {
	    final int r = rl * RADIX + result.mant[i];
	    final int rh = r / divisor;
	    rl = r - rh * divisor;
	    result.mant[i] = rh;
	}

	if (result.mant[mant.length - 1] == 0) {
	    // normalize
	    result.shiftLeft();
	    final int r = rl * RADIX; // compute the next digit and put it in
	    final int rh = r / divisor;
	    rl = r - rh * divisor;
	    result.mant[0] = rh;
	}

	final int excp = result.round(rl * RADIX / divisor); // do the rounding
	if (excp != 0) {
	    result = dotrap(excp, DIVIDE_TRAP, result, result);
	}

	return result;

    }

    /** Check if instance is equal to x.
     * @param other object to check instance against
     * @return true if instance is equal to x and neither are NaN, false otherwise
     */
    @Override
    public boolean equals(final Object other) {

	if (other instanceof Dfp) {
	    final Dfp x = (Dfp) other;
	    if (isNaN() || x.isNaN() || field.getRadixDigits() != x.field.getRadixDigits()) {
		return false;
	    }

	    return compare(this, x) == 0;
	}

	return false;

    }

    /** Copy constructor.
     * @param d instance to copy
     */
    public Dfp(final Dfp d) {
	mant = d.mant.clone();
	sign = d.sign;
	exp = d.exp;
	nans = d.nans;
	field = d.field;
    }

    /** Trap handler.  Subclasses may override this to provide trap
     *  functionality per IEEE 854-1987.
     *
     *  @param type  The exception type - e.g. FLAG_OVERFLOW
     *  @param what  The name of the routine we were in e.g. divide()
     *  @param oper  An operand to this function if any
     *  @param def   The default return value if trap not enabled
     *  @param result    The result that is specified to be delivered per
     *                   IEEE 854, if any
     *  @return the value that should be return by the operation triggering the trap
     */
    protected Dfp trap(int type, String what, Dfp oper, Dfp def, Dfp result) {
	return def;
    }

    /** Check if instance is not a number.
     * @return true if instance is not a number
     */
    public boolean isNaN() {
	return (nans == QNAN) || (nans == SNAN);
    }

    /** Check if instance is greater than x.
     * @param x number to check instance against
     * @return true if instance is greater than x and neither are NaN, false otherwise
     */
    public boolean greaterThan(final Dfp x) {

	// make sure we don't mix number with different precision
	if (field.getRadixDigits() != x.field.getRadixDigits()) {
	    field.setIEEEFlagsBits(DfpField.FLAG_INVALID);
	    final Dfp result = newInstance(getZero());
	    result.nans = QNAN;
	    dotrap(DfpField.FLAG_INVALID, GREATER_THAN_TRAP, x, result);
	    return false;
	}

	/* if a nan is involved, signal invalid and return false */
	if (isNaN() || x.isNaN()) {
	    field.setIEEEFlagsBits(DfpField.FLAG_INVALID);
	    dotrap(DfpField.FLAG_INVALID, GREATER_THAN_TRAP, x, newInstance(getZero()));
	    return false;
	}

	return compare(this, x) &gt; 0;
    }

    /** Check if instance is less than x.
     * @param x number to check instance against
     * @return true if instance is less than x and neither are NaN, false otherwise
     */
    public boolean lessThan(final Dfp x) {

	// make sure we don't mix number with different precision
	if (field.getRadixDigits() != x.field.getRadixDigits()) {
	    field.setIEEEFlagsBits(DfpField.FLAG_INVALID);
	    final Dfp result = newInstance(getZero());
	    result.nans = QNAN;
	    dotrap(DfpField.FLAG_INVALID, LESS_THAN_TRAP, x, result);
	    return false;
	}

	/* if a nan is involved, signal invalid and return false */
	if (isNaN() || x.isNaN()) {
	    field.setIEEEFlagsBits(DfpField.FLAG_INVALID);
	    dotrap(DfpField.FLAG_INVALID, LESS_THAN_TRAP, x, newInstance(getZero()));
	    return false;
	}

	return compare(this, x) &lt; 0;
    }

    /** Round this given the next digit n using the current rounding mode.
     * @param n ???
     * @return the IEEE flag if an exception occurred
     */
    protected int round(int n) {
	boolean inc = false;
	switch (field.getRoundingMode()) {
	case ROUND_DOWN:
	    inc = false;
	    break;

	case ROUND_UP:
	    inc = n != 0; // round up if n!=0
	    break;

	case ROUND_HALF_UP:
	    inc = n &gt;= 5000; // round half up
	    break;

	case ROUND_HALF_DOWN:
	    inc = n &gt; 5000; // round half down
	    break;

	case ROUND_HALF_EVEN:
	    inc = n &gt; 5000 || (n == 5000 && (mant[0] & 1) == 1); // round half-even
	    break;

	case ROUND_HALF_ODD:
	    inc = n &gt; 5000 || (n == 5000 && (mant[0] & 1) == 0); // round half-odd
	    break;

	case ROUND_CEIL:
	    inc = sign == 1 && n != 0; // round ceil
	    break;

	case ROUND_FLOOR:
	default:
	    inc = sign == -1 && n != 0; // round floor
	    break;
	}

	if (inc) {
	    // increment if necessary
	    int rh = 1;
	    for (int i = 0; i &lt; mant.length; i++) {
		final int r = mant[i] + rh;
		rh = r / RADIX;
		mant[i] = r - rh * RADIX;
	    }

	    if (rh != 0) {
		shiftRight();
		mant[mant.length - 1] = rh;
	    }
	}

	// check for exceptional cases and raise signals if necessary
	if (exp &lt; MIN_EXP) {
	    // Gradual Underflow
	    field.setIEEEFlagsBits(DfpField.FLAG_UNDERFLOW);
	    return DfpField.FLAG_UNDERFLOW;
	}

	if (exp &gt; MAX_EXP) {
	    // Overflow
	    field.setIEEEFlagsBits(DfpField.FLAG_OVERFLOW);
	    return DfpField.FLAG_OVERFLOW;
	}

	if (n != 0) {
	    // Inexact
	    field.setIEEEFlagsBits(DfpField.FLAG_INEXACT);
	    return DfpField.FLAG_INEXACT;
	}

	return 0;

    }

    /** Compare two instances.
     * @param a first instance in comparison
     * @param b second instance in comparison
     * @return -1 if a&lt;b, 1 if a&gt;b and 0 if a==b
     *  Note this method does not properly handle NaNs or numbers with different precision.
     */
    private static int compare(final Dfp a, final Dfp b) {
	// Ignore the sign of zero
	if (a.mant[a.mant.length - 1] == 0 && b.mant[b.mant.length - 1] == 0 && a.nans == FINITE && b.nans == FINITE) {
	    return 0;
	}

	if (a.sign != b.sign) {
	    if (a.sign == -1) {
		return -1;
	    } else {
		return 1;
	    }
	}

	// deal with the infinities
	if (a.nans == INFINITE && b.nans == FINITE) {
	    return a.sign;
	}

	if (a.nans == FINITE && b.nans == INFINITE) {
	    return -b.sign;
	}

	if (a.nans == INFINITE && b.nans == INFINITE) {
	    return 0;
	}

	// Handle special case when a or b is zero, by ignoring the exponents
	if (b.mant[b.mant.length - 1] != 0 && a.mant[b.mant.length - 1] != 0) {
	    if (a.exp &lt; b.exp) {
		return -a.sign;
	    }

	    if (a.exp &gt; b.exp) {
		return a.sign;
	    }
	}

	// compare the mantissas
	for (int i = a.mant.length - 1; i &gt;= 0; i--) {
	    if (a.mant[i] &gt; b.mant[i]) {
		return a.sign;
	    }

	    if (a.mant[i] &lt; b.mant[i]) {
		return -a.sign;
	    }
	}

	return 0;

    }

    /** Make our exp equal to the supplied one, this may cause rounding.
     *  Also causes de-normalized numbers.  These numbers are generally
     *  dangerous because most routines assume normalized numbers.
     *  Align doesn't round, so it will return the last digit destroyed
     *  by shifting right.
     *  @param e desired exponent
     *  @return last digit destroyed by shifting right
     */
    protected int align(int e) {
	int lostdigit = 0;
	boolean inexact = false;

	int diff = exp - e;

	int adiff = diff;
	if (adiff &lt; 0) {
	    adiff = -adiff;
	}

	if (diff == 0) {
	    return 0;
	}

	if (adiff &gt; (mant.length + 1)) {
	    // Special case
	    Arrays.fill(mant, 0);
	    exp = e;

	    field.setIEEEFlagsBits(DfpField.FLAG_INEXACT);
	    dotrap(DfpField.FLAG_INEXACT, ALIGN_TRAP, this, this);

	    return 0;
	}

	for (int i = 0; i &lt; adiff; i++) {
	    if (diff &lt; 0) {
		/* Keep track of loss -- only signal inexact after losing 2 digits.
		 * the first lost digit is returned to add() and may be incorporated
		 * into the result.
		 */
		if (lostdigit != 0) {
		    inexact = true;
		}

		lostdigit = mant[0];

		shiftRight();
	    } else {
		shiftLeft();
	    }
	}

	if (inexact) {
	    field.setIEEEFlagsBits(DfpField.FLAG_INEXACT);
	    dotrap(DfpField.FLAG_INEXACT, ALIGN_TRAP, this, this);
	}

	return lostdigit;

    }

    /** Negate the mantissa of this by computing the complement.
     *  Leaves the sign bit unchanged, used internally by add.
     *  Denormalized numbers are handled properly here.
     *  @param extra ???
     *  @return ???
     */
    protected int complement(int extra) {

	extra = RADIX - extra;
	for (int i = 0; i &lt; mant.length; i++) {
	    mant[i] = RADIX - mant[i] - 1;
	}

	int rh = extra / RADIX;
	extra -= rh * RADIX;
	for (int i = 0; i &lt; mant.length; i++) {
	    final int r = mant[i] + rh;
	    rh = r / RADIX;
	    mant[i] = r - rh * RADIX;
	}

	return extra;
    }

    /** Shift the mantissa right, and adjust the exponent to compensate.
     */
    protected void shiftRight() {
	for (int i = 0; i &lt; mant.length - 1; i++) {
	    mant[i] = mant[i + 1];
	}
	mant[mant.length - 1] = 0;
	exp++;
    }

    /** Shift the mantissa left, and adjust the exponent to compensate.
     */
    protected void shiftLeft() {
	for (int i = mant.length - 1; i &gt; 0; i--) {
	    mant[i] = mant[i - 1];
	}
	mant[0] = 0;
	exp--;
    }

}

