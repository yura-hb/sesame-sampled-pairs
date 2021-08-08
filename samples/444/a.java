class FastMath {
    /**
     * Sine function.
     *
     * @param x Argument.
     * @return sin(x)
     */
    public static double sin(double x) {
	boolean negative = false;
	int quadrant = 0;
	double xa;
	double xb = 0.0;

	/* Take absolute value of the input */
	xa = x;
	if (x &lt; 0) {
	    negative = true;
	    xa = -xa;
	}

	/* Check for zero and negative zero */
	if (xa == 0.0) {
	    long bits = Double.doubleToRawLongBits(x);
	    if (bits &lt; 0) {
		return -0.0;
	    }
	    return 0.0;
	}

	if (xa != xa || xa == Double.POSITIVE_INFINITY) {
	    return Double.NaN;
	}

	/* Perform any argument reduction */
	if (xa &gt; 3294198.0) {
	    // PI * (2**20)
	    // Argument too big for CodyWaite reduction.  Must use
	    // PayneHanek.
	    double reduceResults[] = new double[3];
	    reducePayneHanek(xa, reduceResults);
	    quadrant = ((int) reduceResults[0]) & 3;
	    xa = reduceResults[1];
	    xb = reduceResults[2];
	} else if (xa &gt; 1.5707963267948966) {
	    final CodyWaite cw = new CodyWaite(xa);
	    quadrant = cw.getK() & 3;
	    xa = cw.getRemA();
	    xb = cw.getRemB();
	}

	if (negative) {
	    quadrant ^= 2; // Flip bit 1
	}

	switch (quadrant) {
	case 0:
	    return sinQ(xa, xb);
	case 1:
	    return cosQ(xa, xb);
	case 2:
	    return -sinQ(xa, xb);
	case 3:
	    return -cosQ(xa, xb);
	default:
	    return Double.NaN;
	}
    }

    /** Bits of 1/(2*pi), need for reducePayneHanek(). */
    private static final long RECIP_2PI[] = new long[] { (0x28be60dbL &lt;&lt; 32) | 0x9391054aL,
	    (0x7f09d5f4L &lt;&lt; 32) | 0x7d4d3770L, (0x36d8a566L &lt;&lt; 32) | 0x4f10e410L, (0x7f9458eaL &lt;&lt; 32) | 0xf7aef158L,
	    (0x6dc91b8eL &lt;&lt; 32) | 0x909374b8L, (0x01924bbaL &lt;&lt; 32) | 0x82746487L, (0x3f877ac7L &lt;&lt; 32) | 0x2c4a69cfL,
	    (0xba208d7dL &lt;&lt; 32) | 0x4baed121L, (0x3a671c09L &lt;&lt; 32) | 0xad17df90L, (0x4e64758eL &lt;&lt; 32) | 0x60d4ce7dL,
	    (0x272117e2L &lt;&lt; 32) | 0xef7e4a0eL, (0xc7fe25ffL &lt;&lt; 32) | 0xf7816603L, (0xfbcbc462L &lt;&lt; 32) | 0xd6829b47L,
	    (0xdb4d9fb3L &lt;&lt; 32) | 0xc9f2c26dL, (0xd3d18fd9L &lt;&lt; 32) | 0xa797fa8bL, (0x5d49eeb1L &lt;&lt; 32) | 0xfaf97c5eL,
	    (0xcf41ce7dL &lt;&lt; 32) | 0xe294a4baL, 0x9afed7ecL &lt;&lt; 32 };
    /** Bits of pi/4, need for reducePayneHanek(). */
    private static final long PI_O_4_BITS[] = new long[] { (0xc90fdaa2L &lt;&lt; 32) | 0x2168c234L,
	    (0xc4c6628bL &lt;&lt; 32) | 0x80dc1cd1L };
    /** 2^52 - double numbers this large must be integral (no fraction) or NaN or Infinite */
    private static final double TWO_POWER_52 = 4503599627370496.0;
    /** Eighths.
     * This is used by sinQ, because its faster to do a table lookup than
     * a multiply in this time-critical routine
     */
    private static final double EIGHTHS[] = { 0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0, 1.125, 1.25, 1.375,
	    1.5, 1.625 };
    /** Sine table (high bits). */
    private static final double SINE_TABLE_A[] = { +0.0d, +0.1246747374534607d, +0.24740394949913025d,
	    +0.366272509098053d, +0.4794255495071411d, +0.5850973129272461d, +0.6816387176513672d, +0.7675435543060303d,
	    +0.8414709568023682d, +0.902267575263977d, +0.9489846229553223d, +0.9808930158615112d, +0.9974949359893799d,
	    +0.9985313415527344d, };
    /** Sine table (low bits). */
    private static final double SINE_TABLE_B[] = { +0.0d, -4.068233003401932E-9d, +9.755392680573412E-9d,
	    +1.9987994582857286E-8d, -1.0902938113007961E-8d, -3.9986783938944604E-8d, +4.23719669792332E-8d,
	    -5.207000323380292E-8d, +2.800552834259E-8d, +1.883511811213715E-8d, -3.5997360512765566E-9d,
	    +4.116164446561962E-8d, +5.0614674548127384E-8d, -1.0129027912496858E-9d, };
    /** Cosine table (high bits). */
    private static final double COSINE_TABLE_A[] = { +1.0d, +0.9921976327896118d, +0.9689123630523682d,
	    +0.9305076599121094d, +0.8775825500488281d, +0.8109631538391113d, +0.7316888570785522d,
	    +0.6409968137741089d, +0.5403022766113281d, +0.4311765432357788d, +0.3153223395347595d,
	    +0.19454771280288696d, +0.07073719799518585d, -0.05417713522911072d, };
    /** Cosine table (low bits). */
    private static final double COSINE_TABLE_B[] = { +0.0d, +3.4439717236742845E-8d, +5.865827662008209E-8d,
	    -3.7999795083850525E-8d, +1.184154459111628E-8d, -3.43338934259355E-8d, +1.1795268640216787E-8d,
	    +4.438921624363781E-8d, +2.925681159240093E-8d, -2.6437112632041807E-8d, +2.2860509143963117E-8d,
	    -4.813899778443457E-9d, +3.6725170580355583E-9d, +2.0217439756338078E-10d, };
    /**
     * 0x40000000 - used to split a double into two parts, both with the low order bits cleared.
     * Equivalent to 2^30.
     */
    private static final long HEX_40000000 = 0x40000000L;

    /** Reduce the input argument using the Payne and Hanek method.
     *  This is good for all inputs 0.0 &lt; x &lt; inf
     *  Output is remainder after dividing by PI/2
     *  The result array should contain 3 numbers.
     *  result[0] is the integer portion, so mod 4 this gives the quadrant.
     *  result[1] is the upper bits of the remainder
     *  result[2] is the lower bits of the remainder
     *
     * @param x number to reduce
     * @param result placeholder where to put the result
     */
    private static void reducePayneHanek(double x, double result[]) {
	/* Convert input double to bits */
	long inbits = Double.doubleToRawLongBits(x);
	int exponent = (int) ((inbits &gt;&gt; 52) & 0x7ff) - 1023;

	/* Convert to fixed point representation */
	inbits &= 0x000fffffffffffffL;
	inbits |= 0x0010000000000000L;

	/* Normalize input to be between 0.5 and 1.0 */
	exponent++;
	inbits &lt;&lt;= 11;

	/* Based on the exponent, get a shifted copy of recip2pi */
	long shpi0;
	long shpiA;
	long shpiB;
	int idx = exponent &gt;&gt; 6;
	int shift = exponent - (idx &lt;&lt; 6);

	if (shift != 0) {
	    shpi0 = (idx == 0) ? 0 : (RECIP_2PI[idx - 1] &lt;&lt; shift);
	    shpi0 |= RECIP_2PI[idx] &gt;&gt;&gt; (64 - shift);
	    shpiA = (RECIP_2PI[idx] &lt;&lt; shift) | (RECIP_2PI[idx + 1] &gt;&gt;&gt; (64 - shift));
	    shpiB = (RECIP_2PI[idx + 1] &lt;&lt; shift) | (RECIP_2PI[idx + 2] &gt;&gt;&gt; (64 - shift));
	} else {
	    shpi0 = (idx == 0) ? 0 : RECIP_2PI[idx - 1];
	    shpiA = RECIP_2PI[idx];
	    shpiB = RECIP_2PI[idx + 1];
	}

	/* Multiply input by shpiA */
	long a = inbits &gt;&gt;&gt; 32;
	long b = inbits & 0xffffffffL;

	long c = shpiA &gt;&gt;&gt; 32;
	long d = shpiA & 0xffffffffL;

	long ac = a * c;
	long bd = b * d;
	long bc = b * c;
	long ad = a * d;

	long prodB = bd + (ad &lt;&lt; 32);
	long prodA = ac + (ad &gt;&gt;&gt; 32);

	boolean bita = (bd & 0x8000000000000000L) != 0;
	boolean bitb = (ad & 0x80000000L) != 0;
	boolean bitsum = (prodB & 0x8000000000000000L) != 0;

	/* Carry */
	if ((bita && bitb) || ((bita || bitb) && !bitsum)) {
	    prodA++;
	}

	bita = (prodB & 0x8000000000000000L) != 0;
	bitb = (bc & 0x80000000L) != 0;

	prodB += bc &lt;&lt; 32;
	prodA += bc &gt;&gt;&gt; 32;

	bitsum = (prodB & 0x8000000000000000L) != 0;

	/* Carry */
	if ((bita && bitb) || ((bita || bitb) && !bitsum)) {
	    prodA++;
	}

	/* Multiply input by shpiB */
	c = shpiB &gt;&gt;&gt; 32;
	d = shpiB & 0xffffffffL;
	ac = a * c;
	bc = b * c;
	ad = a * d;

	/* Collect terms */
	ac += (bc + ad) &gt;&gt;&gt; 32;

	bita = (prodB & 0x8000000000000000L) != 0;
	bitb = (ac & 0x8000000000000000L) != 0;
	prodB += ac;
	bitsum = (prodB & 0x8000000000000000L) != 0;
	/* Carry */
	if ((bita && bitb) || ((bita || bitb) && !bitsum)) {
	    prodA++;
	}

	/* Multiply by shpi0 */
	c = shpi0 &gt;&gt;&gt; 32;
	d = shpi0 & 0xffffffffL;

	bd = b * d;
	bc = b * c;
	ad = a * d;

	prodA += bd + ((bc + ad) &lt;&lt; 32);

	/*
	 * prodA, prodB now contain the remainder as a fraction of PI.  We want this as a fraction of
	 * PI/2, so use the following steps:
	 * 1.) multiply by 4.
	 * 2.) do a fixed point muliply by PI/4.
	 * 3.) Convert to floating point.
	 * 4.) Multiply by 2
	 */

	/* This identifies the quadrant */
	int intPart = (int) (prodA &gt;&gt;&gt; 62);

	/* Multiply by 4 */
	prodA &lt;&lt;= 2;
	prodA |= prodB &gt;&gt;&gt; 62;
	prodB &lt;&lt;= 2;

	/* Multiply by PI/4 */
	a = prodA &gt;&gt;&gt; 32;
	b = prodA & 0xffffffffL;

	c = PI_O_4_BITS[0] &gt;&gt;&gt; 32;
	d = PI_O_4_BITS[0] & 0xffffffffL;

	ac = a * c;
	bd = b * d;
	bc = b * c;
	ad = a * d;

	long prod2B = bd + (ad &lt;&lt; 32);
	long prod2A = ac + (ad &gt;&gt;&gt; 32);

	bita = (bd & 0x8000000000000000L) != 0;
	bitb = (ad & 0x80000000L) != 0;
	bitsum = (prod2B & 0x8000000000000000L) != 0;

	/* Carry */
	if ((bita && bitb) || ((bita || bitb) && !bitsum)) {
	    prod2A++;
	}

	bita = (prod2B & 0x8000000000000000L) != 0;
	bitb = (bc & 0x80000000L) != 0;

	prod2B += bc &lt;&lt; 32;
	prod2A += bc &gt;&gt;&gt; 32;

	bitsum = (prod2B & 0x8000000000000000L) != 0;

	/* Carry */
	if ((bita && bitb) || ((bita || bitb) && !bitsum)) {
	    prod2A++;
	}

	/* Multiply input by pio4bits[1] */
	c = PI_O_4_BITS[1] &gt;&gt;&gt; 32;
	d = PI_O_4_BITS[1] & 0xffffffffL;
	ac = a * c;
	bc = b * c;
	ad = a * d;

	/* Collect terms */
	ac += (bc + ad) &gt;&gt;&gt; 32;

	bita = (prod2B & 0x8000000000000000L) != 0;
	bitb = (ac & 0x8000000000000000L) != 0;
	prod2B += ac;
	bitsum = (prod2B & 0x8000000000000000L) != 0;
	/* Carry */
	if ((bita && bitb) || ((bita || bitb) && !bitsum)) {
	    prod2A++;
	}

	/* Multiply inputB by pio4bits[0] */
	a = prodB &gt;&gt;&gt; 32;
	b = prodB & 0xffffffffL;
	c = PI_O_4_BITS[0] &gt;&gt;&gt; 32;
	d = PI_O_4_BITS[0] & 0xffffffffL;
	ac = a * c;
	bc = b * c;
	ad = a * d;

	/* Collect terms */
	ac += (bc + ad) &gt;&gt;&gt; 32;

	bita = (prod2B & 0x8000000000000000L) != 0;
	bitb = (ac & 0x8000000000000000L) != 0;
	prod2B += ac;
	bitsum = (prod2B & 0x8000000000000000L) != 0;
	/* Carry */
	if ((bita && bitb) || ((bita || bitb) && !bitsum)) {
	    prod2A++;
	}

	/* Convert to double */
	double tmpA = (prod2A &gt;&gt;&gt; 12) / TWO_POWER_52; // High order 52 bits
	double tmpB = (((prod2A & 0xfffL) &lt;&lt; 40) + (prod2B &gt;&gt;&gt; 24)) / TWO_POWER_52 / TWO_POWER_52; // Low bits

	double sumA = tmpA + tmpB;
	double sumB = -(sumA - tmpA - tmpB);

	/* Multiply by PI/2 and return */
	result[0] = intPart;
	result[1] = sumA * 2.0;
	result[2] = sumB * 2.0;
    }

    /**
     *  Compute sine over the first quadrant (0 &lt; x &lt; pi/2).
     *  Use combination of table lookup and rational polynomial expansion.
     *  @param xa number from which sine is requested
     *  @param xb extra bits for x (may be 0.0)
     *  @return sin(xa + xb)
     */
    private static double sinQ(double xa, double xb) {
	int idx = (int) ((xa * 8.0) + 0.5);
	final double epsilon = xa - EIGHTHS[idx]; //idx*0.125;

	// Table lookups
	final double sintA = SINE_TABLE_A[idx];
	final double sintB = SINE_TABLE_B[idx];
	final double costA = COSINE_TABLE_A[idx];
	final double costB = COSINE_TABLE_B[idx];

	// Polynomial eval of sin(epsilon), cos(epsilon)
	double sinEpsA = epsilon;
	double sinEpsB = polySine(epsilon);
	final double cosEpsA = 1.0;
	final double cosEpsB = polyCosine(epsilon);

	// Split epsilon   xa + xb = x
	final double temp = sinEpsA * HEX_40000000;
	double temp2 = (sinEpsA + temp) - temp;
	sinEpsB += sinEpsA - temp2;
	sinEpsA = temp2;

	/* Compute sin(x) by angle addition formula */
	double result;

	/* Compute the following sum:
	 *
	 * result = sintA + costA*sinEpsA + sintA*cosEpsB + costA*sinEpsB +
	 *          sintB + costB*sinEpsA + sintB*cosEpsB + costB*sinEpsB;
	 *
	 * Ranges of elements
	 *
	 * xxxtA   0            PI/2
	 * xxxtB   -1.5e-9      1.5e-9
	 * sinEpsA -0.0625      0.0625
	 * sinEpsB -6e-11       6e-11
	 * cosEpsA  1.0
	 * cosEpsB  0           -0.0625
	 *
	 */

	//result = sintA + costA*sinEpsA + sintA*cosEpsB + costA*sinEpsB +
	//          sintB + costB*sinEpsA + sintB*cosEpsB + costB*sinEpsB;

	//result = sintA + sintA*cosEpsB + sintB + sintB * cosEpsB;
	//result += costA*sinEpsA + costA*sinEpsB + costB*sinEpsA + costB * sinEpsB;
	double a = 0;
	double b = 0;

	double t = sintA;
	double c = a + t;
	double d = -(c - a - t);
	a = c;
	b += d;

	t = costA * sinEpsA;
	c = a + t;
	d = -(c - a - t);
	a = c;
	b += d;

	b = b + sintA * cosEpsB + costA * sinEpsB;
	/*
	t = sintA*cosEpsB;
	c = a + t;
	d = -(c - a - t);
	a = c;
	b = b + d;
	
	t = costA*sinEpsB;
	c = a + t;
	d = -(c - a - t);
	a = c;
	b = b + d;
	 */

	b = b + sintB + costB * sinEpsA + sintB * cosEpsB + costB * sinEpsB;
	/*
	t = sintB;
	c = a + t;
	d = -(c - a - t);
	a = c;
	b = b + d;
	
	t = costB*sinEpsA;
	c = a + t;
	d = -(c - a - t);
	a = c;
	b = b + d;
	
	t = sintB*cosEpsB;
	c = a + t;
	d = -(c - a - t);
	a = c;
	b = b + d;
	
	t = costB*sinEpsB;
	c = a + t;
	d = -(c - a - t);
	a = c;
	b = b + d;
	 */

	if (xb != 0.0) {
	    t = ((costA + costB) * (cosEpsA + cosEpsB) - (sintA + sintB) * (sinEpsA + sinEpsB)) * xb; // approximate cosine*xb
	    c = a + t;
	    d = -(c - a - t);
	    a = c;
	    b += d;
	}

	result = a + b;

	return result;
    }

    /**
     * Compute cosine in the first quadrant by subtracting input from PI/2 and
     * then calling sinQ.  This is more accurate as the input approaches PI/2.
     *  @param xa number from which cosine is requested
     *  @param xb extra bits for x (may be 0.0)
     *  @return cos(xa + xb)
     */
    private static double cosQ(double xa, double xb) {
	final double pi2a = 1.5707963267948966;
	final double pi2b = 6.123233995736766E-17;

	final double a = pi2a - xa;
	double b = -(a - pi2a + xa);
	b += pi2b - xb;

	return sinQ(a, b);
    }

    /**
     *  Computes sin(x) - x, where |x| &lt; 1/16.
     *  Use a Remez polynomial approximation.
     *  @param x a number smaller than 1/16
     *  @return sin(x) - x
     */
    private static double polySine(final double x) {
	double x2 = x * x;

	double p = 2.7553817452272217E-6;
	p = p * x2 + -1.9841269659586505E-4;
	p = p * x2 + 0.008333333333329196;
	p = p * x2 + -0.16666666666666666;
	//p *= x2;
	//p *= x;
	p = p * x2 * x;

	return p;
    }

    /**
     *  Computes cos(x) - 1, where |x| &lt; 1/16.
     *  Use a Remez polynomial approximation.
     *  @param x a number smaller than 1/16
     *  @return cos(x) - 1
     */
    private static double polyCosine(double x) {
	double x2 = x * x;

	double p = 2.479773539153719E-5;
	p = p * x2 + -0.0013888888689039883;
	p = p * x2 + 0.041666666666621166;
	p = p * x2 + -0.49999999999999994;
	p *= x2;

	return p;
    }

    class CodyWaite {
	/** Bits of 1/(2*pi), need for reducePayneHanek(). */
	private static final long RECIP_2PI[] = new long[] { (0x28be60dbL &lt;&lt; 32) | 0x9391054aL,
		(0x7f09d5f4L &lt;&lt; 32) | 0x7d4d3770L, (0x36d8a566L &lt;&lt; 32) | 0x4f10e410L, (0x7f9458eaL &lt;&lt; 32) | 0xf7aef158L,
		(0x6dc91b8eL &lt;&lt; 32) | 0x909374b8L, (0x01924bbaL &lt;&lt; 32) | 0x82746487L, (0x3f877ac7L &lt;&lt; 32) | 0x2c4a69cfL,
		(0xba208d7dL &lt;&lt; 32) | 0x4baed121L, (0x3a671c09L &lt;&lt; 32) | 0xad17df90L, (0x4e64758eL &lt;&lt; 32) | 0x60d4ce7dL,
		(0x272117e2L &lt;&lt; 32) | 0xef7e4a0eL, (0xc7fe25ffL &lt;&lt; 32) | 0xf7816603L, (0xfbcbc462L &lt;&lt; 32) | 0xd6829b47L,
		(0xdb4d9fb3L &lt;&lt; 32) | 0xc9f2c26dL, (0xd3d18fd9L &lt;&lt; 32) | 0xa797fa8bL, (0x5d49eeb1L &lt;&lt; 32) | 0xfaf97c5eL,
		(0xcf41ce7dL &lt;&lt; 32) | 0xe294a4baL, 0x9afed7ecL &lt;&lt; 32 };
	/** Bits of pi/4, need for reducePayneHanek(). */
	private static final long PI_O_4_BITS[] = new long[] { (0xc90fdaa2L &lt;&lt; 32) | 0x2168c234L,
		(0xc4c6628bL &lt;&lt; 32) | 0x80dc1cd1L };
	/** 2^52 - double numbers this large must be integral (no fraction) or NaN or Infinite */
	private static final double TWO_POWER_52 = 4503599627370496.0;
	/** Eighths.
	* This is used by sinQ, because its faster to do a table lookup than
	* a multiply in this time-critical routine
	*/
	private static final double EIGHTHS[] = { 0, 0.125, 0.25, 0.375, 0.5, 0.625, 0.75, 0.875, 1.0, 1.125, 1.25,
		1.375, 1.5, 1.625 };
	/** Sine table (high bits). */
	private static final double SINE_TABLE_A[] = { +0.0d, +0.1246747374534607d, +0.24740394949913025d,
		+0.366272509098053d, +0.4794255495071411d, +0.5850973129272461d, +0.6816387176513672d,
		+0.7675435543060303d, +0.8414709568023682d, +0.902267575263977d, +0.9489846229553223d,
		+0.9808930158615112d, +0.9974949359893799d, +0.9985313415527344d, };
	/** Sine table (low bits). */
	private static final double SINE_TABLE_B[] = { +0.0d, -4.068233003401932E-9d, +9.755392680573412E-9d,
		+1.9987994582857286E-8d, -1.0902938113007961E-8d, -3.9986783938944604E-8d, +4.23719669792332E-8d,
		-5.207000323380292E-8d, +2.800552834259E-8d, +1.883511811213715E-8d, -3.5997360512765566E-9d,
		+4.116164446561962E-8d, +5.0614674548127384E-8d, -1.0129027912496858E-9d, };
	/** Cosine table (high bits). */
	private static final double COSINE_TABLE_A[] = { +1.0d, +0.9921976327896118d, +0.9689123630523682d,
		+0.9305076599121094d, +0.8775825500488281d, +0.8109631538391113d, +0.7316888570785522d,
		+0.6409968137741089d, +0.5403022766113281d, +0.4311765432357788d, +0.3153223395347595d,
		+0.19454771280288696d, +0.07073719799518585d, -0.05417713522911072d, };
	/** Cosine table (low bits). */
	private static final double COSINE_TABLE_B[] = { +0.0d, +3.4439717236742845E-8d, +5.865827662008209E-8d,
		-3.7999795083850525E-8d, +1.184154459111628E-8d, -3.43338934259355E-8d, +1.1795268640216787E-8d,
		+4.438921624363781E-8d, +2.925681159240093E-8d, -2.6437112632041807E-8d, +2.2860509143963117E-8d,
		-4.813899778443457E-9d, +3.6725170580355583E-9d, +2.0217439756338078E-10d, };
	/**
	* 0x40000000 - used to split a double into two parts, both with the low order bits cleared.
	* Equivalent to 2^30.
	*/
	private static final long HEX_40000000 = 0x40000000L;

	/**
	 * @param xa Argument.
	 */
	CodyWaite(double xa) {
	    // Estimate k.
	    //k = (int)(xa / 1.5707963267948966);
	    int k = (int) (xa * 0.6366197723675814);

	    // Compute remainder.
	    double remA;
	    double remB;
	    while (true) {
		double a = -k * 1.570796251296997;
		remA = xa + a;
		remB = -(remA - xa - a);

		a = -k * 7.549789948768648E-8;
		double b = remA;
		remA = a + b;
		remB += -(remA - b - a);

		a = -k * 6.123233995736766E-17;
		b = remA;
		remA = a + b;
		remB += -(remA - b - a);

		if (remA &gt; 0) {
		    break;
		}

		// Remainder is negative, so decrement k and try again.
		// This should only happen if the input is very close
		// to an even multiple of pi/2.
		--k;
	    }

	    this.finalK = k;
	    this.finalRemA = remA;
	    this.finalRemB = remB;
	}

	/**
	 * @return k
	 */
	int getK() {
	    return finalK;
	}

	/**
	 * @return remA
	 */
	double getRemA() {
	    return finalRemA;
	}

	/**
	 * @return remB
	 */
	double getRemB() {
	    return finalRemB;
	}

    }

}

