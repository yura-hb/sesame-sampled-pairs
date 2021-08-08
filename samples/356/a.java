class Dfp implements RealFieldElement&lt;Dfp&gt; {
    /** Get the specified  power of 10000.
     * @param e desired power
     * @return 10000&lt;sup&gt;e&lt;/sup&gt;
     */
    public Dfp power10K(final int e) {
	Dfp d = newInstance(getOne());
	d.exp = e + 1;
	return d;
    }

    /** Exponent. */
    protected int exp;
    /** Factory building similar Dfp's. */
    private final DfpField field;
    /** Indicator for non-finite / non-number values. */
    protected byte nans;
    /** Indicator value for quiet NaN. */
    public static final byte QNAN = 3;
    /** Name for traps triggered by newInstance. */
    private static final String NEW_INSTANCE_TRAP = "newInstance";
    /** Sign bit: 1 for positive, -1 for negative. */
    protected byte sign;
    /** Indicator value for normal finite numbers. */
    public static final byte FINITE = 0;
    /** Mantissa. */
    protected int[] mant;
    /** Indicator value for Infinity. */
    public static final byte INFINITE = 1;
    /** Indicator value for signaling NaN. */
    public static final byte SNAN = 2;
    /** The minimum exponent before underflow is signaled.  Flush to zero
     *  occurs at minExp-DIGITS */
    public static final int MIN_EXP = -32767;
    /** The amount under/overflows are scaled by before going to trap handler */
    public static final int ERR_SCALE = 32760;

    /** Get the constant 1.
     * @return a Dfp with value one
     */
    public Dfp getOne() {
	return field.getOne();
    }

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

    /** Get the constant 0.
     * @return a Dfp with value zero
     */
    public Dfp getZero() {
	return field.getZero();
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

}

