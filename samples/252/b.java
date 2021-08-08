import java.util.Arrays;
import java.util.Objects;

class BigInteger extends Number implements Comparable&lt;BigInteger&gt; {
    /**
     * Returns a BigInteger whose value is &lt;code&gt;(this&lt;sup&gt;exponent&lt;/sup&gt;)&lt;/code&gt;.
     * Note that {@code exponent} is an integer rather than a BigInteger.
     *
     * @param  exponent exponent to which this BigInteger is to be raised.
     * @return &lt;code&gt;this&lt;sup&gt;exponent&lt;/sup&gt;&lt;/code&gt;
     * @throws ArithmeticException {@code exponent} is negative.  (This would
     *         cause the operation to yield a non-integer value.)
     */
    public BigInteger pow(int exponent) {
	if (exponent &lt; 0) {
	    throw new ArithmeticException("Negative exponent");
	}
	if (signum == 0) {
	    return (exponent == 0 ? ONE : this);
	}

	BigInteger partToSquare = this.abs();

	// Factor out powers of two from the base, as the exponentiation of
	// these can be done by left shifts only.
	// The remaining part can then be exponentiated faster.  The
	// powers of two will be multiplied back at the end.
	int powersOfTwo = partToSquare.getLowestSetBit();
	long bitsToShift = (long) powersOfTwo * exponent;
	if (bitsToShift &gt; Integer.MAX_VALUE) {
	    reportOverflow();
	}

	int remainingBits;

	// Factor the powers of two out quickly by shifting right, if needed.
	if (powersOfTwo &gt; 0) {
	    partToSquare = partToSquare.shiftRight(powersOfTwo);
	    remainingBits = partToSquare.bitLength();
	    if (remainingBits == 1) { // Nothing left but +/- 1?
		if (signum &lt; 0 && (exponent & 1) == 1) {
		    return NEGATIVE_ONE.shiftLeft(powersOfTwo * exponent);
		} else {
		    return ONE.shiftLeft(powersOfTwo * exponent);
		}
	    }
	} else {
	    remainingBits = partToSquare.bitLength();
	    if (remainingBits == 1) { // Nothing left but +/- 1?
		if (signum &lt; 0 && (exponent & 1) == 1) {
		    return NEGATIVE_ONE;
		} else {
		    return ONE;
		}
	    }
	}

	// This is a quick way to approximate the size of the result,
	// similar to doing log2[n] * exponent.  This will give an upper bound
	// of how big the result can be, and which algorithm to use.
	long scaleFactor = (long) remainingBits * exponent;

	// Use slightly different algorithms for small and large operands.
	// See if the result will safely fit into a long. (Largest 2^63-1)
	if (partToSquare.mag.length == 1 && scaleFactor &lt;= 62) {
	    // Small number algorithm.  Everything fits into a long.
	    int newSign = (signum &lt; 0 && (exponent & 1) == 1 ? -1 : 1);
	    long result = 1;
	    long baseToPow2 = partToSquare.mag[0] & LONG_MASK;

	    int workingExponent = exponent;

	    // Perform exponentiation using repeated squaring trick
	    while (workingExponent != 0) {
		if ((workingExponent & 1) == 1) {
		    result = result * baseToPow2;
		}

		if ((workingExponent &gt;&gt;&gt;= 1) != 0) {
		    baseToPow2 = baseToPow2 * baseToPow2;
		}
	    }

	    // Multiply back the powers of two (quickly, by shifting left)
	    if (powersOfTwo &gt; 0) {
		if (bitsToShift + scaleFactor &lt;= 62) { // Fits in long?
		    return valueOf((result &lt;&lt; bitsToShift) * newSign);
		} else {
		    return valueOf(result * newSign).shiftLeft((int) bitsToShift);
		}
	    } else {
		return valueOf(result * newSign);
	    }
	} else {
	    // Large number algorithm.  This is basically identical to
	    // the algorithm above, but calls multiply() and square()
	    // which may use more efficient algorithms for large numbers.
	    BigInteger answer = ONE;

	    int workingExponent = exponent;
	    // Perform exponentiation using repeated squaring trick
	    while (workingExponent != 0) {
		if ((workingExponent & 1) == 1) {
		    answer = answer.multiply(partToSquare);
		}

		if ((workingExponent &gt;&gt;&gt;= 1) != 0) {
		    partToSquare = partToSquare.square();
		}
	    }
	    // Multiply back the (exponentiated) powers of two (quickly,
	    // by shifting left)
	    if (powersOfTwo &gt; 0) {
		answer = answer.shiftLeft(powersOfTwo * exponent);
	    }

	    if (signum &lt; 0 && (exponent & 1) == 1) {
		return answer.negate();
	    } else {
		return answer;
	    }
	}
    }

    /**
     * The signum of this BigInteger: -1 for negative, 0 for zero, or
     * 1 for positive.  Note that the BigInteger zero &lt;em&gt;must&lt;/em&gt; have
     * a signum of 0.  This is necessary to ensures that there is exactly one
     * representation for each BigInteger value.
     */
    final int signum;
    /**
     * The BigInteger constant one.
     *
     * @since   1.2
     */
    public static final BigInteger ONE = valueOf(1);
    /**
     * The BigInteger constant -1.  (Not exported.)
     */
    private static final BigInteger NEGATIVE_ONE = valueOf(-1);
    /**
     * The magnitude of this BigInteger, in &lt;i&gt;big-endian&lt;/i&gt; order: the
     * zeroth element of this array is the most-significant int of the
     * magnitude.  The magnitude must be "minimal" in that the most-significant
     * int ({@code mag[0]}) must be non-zero.  This is necessary to
     * ensure that there is exactly one representation for each BigInteger
     * value.  Note that this implies that the BigInteger zero has a
     * zero-length mag array.
     */
    final int[] mag;
    /**
     * This mask is used to obtain the value of an int as if it were unsigned.
     */
    static final long LONG_MASK = 0xffffffffL;
    /**
     * Two plus the lowest set bit of this BigInteger. This is a stable variable.
     *
     * @see #getLowestSetBit
     */
    private int lowestSetBitPlusTwo;
    /**
     * The BigInteger constant zero.
     *
     * @since   1.2
     */
    public static final BigInteger ZERO = new BigInteger(new int[0], 0);
    /**
     * One plus the bitLength of this BigInteger. This is a stable variable.
     * (either value is acceptable).
     *
     * @see #bitLength()
     */
    private int bitLengthPlusOne;
    /**
     * Initialize static constant array when class is loaded.
     */
    private static final int MAX_CONSTANT = 16;
    private static BigInteger posConst[] = new BigInteger[MAX_CONSTANT + 1];
    private static BigInteger negConst[] = new BigInteger[MAX_CONSTANT + 1];
    /**
     * The threshold value for using squaring code to perform multiplication
     * of a {@code BigInteger} instance by itself.  If the number of ints in
     * the number are larger than this value, {@code multiply(this)} will
     * return {@code square()}.
     */
    private static final int MULTIPLY_SQUARE_THRESHOLD = 20;
    /**
     * The threshold value for using Karatsuba multiplication.  If the number
     * of ints in both mag arrays are greater than this number, then
     * Karatsuba multiplication will be used.   This value is found
     * experimentally to work well.
     */
    private static final int KARATSUBA_THRESHOLD = 80;
    /**
     * The threshold value for using 3-way Toom-Cook multiplication.
     * If the number of ints in each mag array is greater than the
     * Karatsuba threshold, and the number of ints in at least one of
     * the mag arrays is greater than this threshold, then Toom-Cook
     * multiplication will be used.
     */
    private static final int TOOM_COOK_THRESHOLD = 240;
    /**
     * The threshold value for using Karatsuba squaring.  If the number
     * of ints in the number are larger than this value,
     * Karatsuba squaring will be used.   This value is found
     * experimentally to work well.
     */
    private static final int KARATSUBA_SQUARE_THRESHOLD = 128;
    /**
     * The threshold value for using Toom-Cook squaring.  If the number
     * of ints in the number are larger than this value,
     * Toom-Cook squaring will be used.   This value is found
     * experimentally to work well.
     */
    private static final int TOOM_COOK_SQUARE_THRESHOLD = 216;
    /**
     * This constant limits {@code mag.length} of BigIntegers to the supported
     * range.
     */
    private static final int MAX_MAG_LENGTH = Integer.MAX_VALUE / Integer.SIZE + 1;
    /**
     * Two plus the index of the lowest-order int in the magnitude of this
     * BigInteger that contains a nonzero int. This is a stable variable. The
     * least significant int has int-number 0, the next int in order of
     * increasing significance has int-number 1, and so forth.
     *
     * &lt;p&gt;Note: never used for a BigInteger with a magnitude of zero.
     *
     * @see #firstNonzeroIntNum()
     */
    private int firstNonzeroIntNumPlusTwo;

    /**
     * Returns a BigInteger whose value is the absolute value of this
     * BigInteger.
     *
     * @return {@code abs(this)}
     */
    public BigInteger abs() {
	return (signum &gt;= 0 ? this : this.negate());
    }

    /**
     * Returns the index of the rightmost (lowest-order) one bit in this
     * BigInteger (the number of zero bits to the right of the rightmost
     * one bit).  Returns -1 if this BigInteger contains no one bits.
     * (Computes {@code (this == 0? -1 : log2(this & -this))}.)
     *
     * @return index of the rightmost one bit in this BigInteger.
     */
    public int getLowestSetBit() {
	int lsb = lowestSetBitPlusTwo - 2;
	if (lsb == -2) { // lowestSetBit not initialized yet
	    lsb = 0;
	    if (signum == 0) {
		lsb -= 1;
	    } else {
		// Search for lowest order nonzero int
		int i, b;
		for (i = 0; (b = getInt(i)) == 0; i++)
		    ;
		lsb += (i &lt;&lt; 5) + Integer.numberOfTrailingZeros(b);
	    }
	    lowestSetBitPlusTwo = lsb + 2;
	}
	return lsb;
    }

    private static void reportOverflow() {
	throw new ArithmeticException("BigInteger would overflow supported range");
    }

    /**
     * Returns a BigInteger whose value is {@code (this &gt;&gt; n)}.  Sign
     * extension is performed.  The shift distance, {@code n}, may be
     * negative, in which case this method performs a left shift.
     * (Computes &lt;code&gt;floor(this / 2&lt;sup&gt;n&lt;/sup&gt;)&lt;/code&gt;.)
     *
     * @param  n shift distance, in bits.
     * @return {@code this &gt;&gt; n}
     * @see #shiftLeft
     */
    public BigInteger shiftRight(int n) {
	if (signum == 0)
	    return ZERO;
	if (n &gt; 0) {
	    return shiftRightImpl(n);
	} else if (n == 0) {
	    return this;
	} else {
	    // Possible int overflow in {@code -n} is not a trouble,
	    // because shiftLeft considers its argument unsigned
	    return new BigInteger(shiftLeft(mag, -n), signum);
	}
    }

    /**
     * Returns the number of bits in the minimal two's-complement
     * representation of this BigInteger, &lt;em&gt;excluding&lt;/em&gt; a sign bit.
     * For positive BigIntegers, this is equivalent to the number of bits in
     * the ordinary binary representation.  For zero this method returns
     * {@code 0}.  (Computes {@code (ceil(log2(this &lt; 0 ? -this : this+1)))}.)
     *
     * @return number of bits in the minimal two's-complement
     *         representation of this BigInteger, &lt;em&gt;excluding&lt;/em&gt; a sign bit.
     */
    public int bitLength() {
	int n = bitLengthPlusOne - 1;
	if (n == -1) { // bitLength not initialized yet
	    int[] m = mag;
	    int len = m.length;
	    if (len == 0) {
		n = 0; // offset by one to initialize
	    } else {
		// Calculate the bit length of the magnitude
		int magBitLength = ((len - 1) &lt;&lt; 5) + bitLengthForInt(mag[0]);
		if (signum &lt; 0) {
		    // Check if magnitude is a power of two
		    boolean pow2 = (Integer.bitCount(mag[0]) == 1);
		    for (int i = 1; i &lt; len && pow2; i++)
			pow2 = (mag[i] == 0);

		    n = (pow2 ? magBitLength - 1 : magBitLength);
		} else {
		    n = magBitLength;
		}
	    }
	    bitLengthPlusOne = n + 1;
	}
	return n;
    }

    /**
     * Returns a BigInteger whose value is {@code (this &lt;&lt; n)}.
     * The shift distance, {@code n}, may be negative, in which case
     * this method performs a right shift.
     * (Computes &lt;code&gt;floor(this * 2&lt;sup&gt;n&lt;/sup&gt;)&lt;/code&gt;.)
     *
     * @param  n shift distance, in bits.
     * @return {@code this &lt;&lt; n}
     * @see #shiftRight
     */
    public BigInteger shiftLeft(int n) {
	if (signum == 0)
	    return ZERO;
	if (n &gt; 0) {
	    return new BigInteger(shiftLeft(mag, n), signum);
	} else if (n == 0) {
	    return this;
	} else {
	    // Possible int overflow in (-n) is not a trouble,
	    // because shiftRightImpl considers its argument unsigned
	    return shiftRightImpl(-n);
	}
    }

    /**
     * Returns a BigInteger whose value is equal to that of the
     * specified {@code long}.
     *
     * @apiNote This static factory method is provided in preference
     * to a ({@code long}) constructor because it allows for reuse of
     * frequently used BigIntegers.
     *
     * @param  val value of the BigInteger to return.
     * @return a BigInteger with the specified value.
     */
    public static BigInteger valueOf(long val) {
	// If -MAX_CONSTANT &lt; val &lt; MAX_CONSTANT, return stashed constant
	if (val == 0)
	    return ZERO;
	if (val &gt; 0 && val &lt;= MAX_CONSTANT)
	    return posConst[(int) val];
	else if (val &lt; 0 && val &gt;= -MAX_CONSTANT)
	    return negConst[(int) -val];

	return new BigInteger(val);
    }

    /**
     * Returns a BigInteger whose value is {@code (this * val)}.
     *
     * @implNote An implementation may offer better algorithmic
     * performance when {@code val == this}.
     *
     * @param  val value to be multiplied by this BigInteger.
     * @return {@code this * val}
     */
    public BigInteger multiply(BigInteger val) {
	if (val.signum == 0 || signum == 0)
	    return ZERO;

	int xlen = mag.length;

	if (val == this && xlen &gt; MULTIPLY_SQUARE_THRESHOLD) {
	    return square();
	}

	int ylen = val.mag.length;

	if ((xlen &lt; KARATSUBA_THRESHOLD) || (ylen &lt; KARATSUBA_THRESHOLD)) {
	    int resultSign = signum == val.signum ? 1 : -1;
	    if (val.mag.length == 1) {
		return multiplyByInt(mag, val.mag[0], resultSign);
	    }
	    if (mag.length == 1) {
		return multiplyByInt(val.mag, mag[0], resultSign);
	    }
	    int[] result = multiplyToLen(mag, xlen, val.mag, ylen, null);
	    result = trustedStripLeadingZeroInts(result);
	    return new BigInteger(result, resultSign);
	} else {
	    if ((xlen &lt; TOOM_COOK_THRESHOLD) && (ylen &lt; TOOM_COOK_THRESHOLD)) {
		return multiplyKaratsuba(this, val);
	    } else {
		return multiplyToomCook3(this, val);
	    }
	}
    }

    /**
     * Returns a BigInteger whose value is {@code (this&lt;sup&gt;2&lt;/sup&gt;)}.
     *
     * @return {@code this&lt;sup&gt;2&lt;/sup&gt;}
     */
    private BigInteger square() {
	if (signum == 0) {
	    return ZERO;
	}
	int len = mag.length;

	if (len &lt; KARATSUBA_SQUARE_THRESHOLD) {
	    int[] z = squareToLen(mag, len, null);
	    return new BigInteger(trustedStripLeadingZeroInts(z), 1);
	} else {
	    if (len &lt; TOOM_COOK_SQUARE_THRESHOLD) {
		return squareKaratsuba();
	    } else {
		return squareToomCook3();
	    }
	}
    }

    /**
     * Returns a BigInteger whose value is {@code (-this)}.
     *
     * @return {@code -this}
     */
    public BigInteger negate() {
	return new BigInteger(this.mag, -this.signum);
    }

    /**
     * Returns the specified int of the little-endian two's complement
     * representation (int 0 is the least significant).  The int number can
     * be arbitrarily high (values are logically preceded by infinitely many
     * sign ints).
     */
    private int getInt(int n) {
	if (n &lt; 0)
	    return 0;
	if (n &gt;= mag.length)
	    return signInt();

	int magInt = mag[mag.length - n - 1];

	return (signum &gt;= 0 ? magInt : (n &lt;= firstNonzeroIntNum() ? -magInt : ~magInt));
    }

    /**
     * Returns a BigInteger whose value is {@code (this &gt;&gt; n)}. The shift
     * distance, {@code n}, is considered unsigned.
     * (Computes &lt;code&gt;floor(this * 2&lt;sup&gt;-n&lt;/sup&gt;)&lt;/code&gt;.)
     *
     * @param  n unsigned shift distance, in bits.
     * @return {@code this &gt;&gt; n}
     */
    private BigInteger shiftRightImpl(int n) {
	int nInts = n &gt;&gt;&gt; 5;
	int nBits = n & 0x1f;
	int magLen = mag.length;
	int newMag[] = null;

	// Special case: entire contents shifted off the end
	if (nInts &gt;= magLen)
	    return (signum &gt;= 0 ? ZERO : negConst[1]);

	if (nBits == 0) {
	    int newMagLen = magLen - nInts;
	    newMag = Arrays.copyOf(mag, newMagLen);
	} else {
	    int i = 0;
	    int highBits = mag[0] &gt;&gt;&gt; nBits;
	    if (highBits != 0) {
		newMag = new int[magLen - nInts];
		newMag[i++] = highBits;
	    } else {
		newMag = new int[magLen - nInts - 1];
	    }

	    int nBits2 = 32 - nBits;
	    int j = 0;
	    while (j &lt; magLen - nInts - 1)
		newMag[i++] = (mag[j++] &lt;&lt; nBits2) | (mag[j] &gt;&gt;&gt; nBits);
	}

	if (signum &lt; 0) {
	    // Find out whether any one-bits were shifted off the end.
	    boolean onesLost = false;
	    for (int i = magLen - 1, j = magLen - nInts; i &gt;= j && !onesLost; i--)
		onesLost = (mag[i] != 0);
	    if (!onesLost && nBits != 0)
		onesLost = (mag[magLen - nInts - 1] &lt;&lt; (32 - nBits) != 0);

	    if (onesLost)
		newMag = javaIncrement(newMag);
	}

	return new BigInteger(newMag, signum);
    }

    /**
     * Returns a magnitude array whose value is {@code (mag &lt;&lt; n)}.
     * The shift distance, {@code n}, is considered unnsigned.
     * (Computes &lt;code&gt;this * 2&lt;sup&gt;n&lt;/sup&gt;&lt;/code&gt;.)
     *
     * @param mag magnitude, the most-significant int ({@code mag[0]}) must be non-zero.
     * @param  n unsigned shift distance, in bits.
     * @return {@code mag &lt;&lt; n}
     */
    private static int[] shiftLeft(int[] mag, int n) {
	int nInts = n &gt;&gt;&gt; 5;
	int nBits = n & 0x1f;
	int magLen = mag.length;
	int newMag[] = null;

	if (nBits == 0) {
	    newMag = new int[magLen + nInts];
	    System.arraycopy(mag, 0, newMag, 0, magLen);
	} else {
	    int i = 0;
	    int nBits2 = 32 - nBits;
	    int highBits = mag[0] &gt;&gt;&gt; nBits2;
	    if (highBits != 0) {
		newMag = new int[magLen + nInts + 1];
		newMag[i++] = highBits;
	    } else {
		newMag = new int[magLen + nInts];
	    }
	    int j = 0;
	    while (j &lt; magLen - 1)
		newMag[i++] = mag[j++] &lt;&lt; nBits | mag[j] &gt;&gt;&gt; nBits2;
	    newMag[i] = mag[j] &lt;&lt; nBits;
	}
	return newMag;
    }

    /**
     * This internal constructor differs from its public cousin
     * with the arguments reversed in two ways: it assumes that its
     * arguments are correct, and it doesn't copy the magnitude array.
     */
    BigInteger(int[] magnitude, int signum) {
	this.signum = (magnitude.length == 0 ? 0 : signum);
	this.mag = magnitude;
	if (mag.length &gt;= MAX_MAG_LENGTH) {
	    checkRange();
	}
    }

    /**
     * Package private method to return bit length for an integer.
     */
    static int bitLengthForInt(int n) {
	return 32 - Integer.numberOfLeadingZeros(n);
    }

    /**
     * Constructs a BigInteger with the specified value, which may not be zero.
     */
    private BigInteger(long val) {
	if (val &lt; 0) {
	    val = -val;
	    signum = -1;
	} else {
	    signum = 1;
	}

	int highWord = (int) (val &gt;&gt;&gt; 32);
	if (highWord == 0) {
	    mag = new int[1];
	    mag[0] = (int) val;
	} else {
	    mag = new int[2];
	    mag[0] = highWord;
	    mag[1] = (int) val;
	}
    }

    private static BigInteger multiplyByInt(int[] x, int y, int sign) {
	if (Integer.bitCount(y) == 1) {
	    return new BigInteger(shiftLeft(x, Integer.numberOfTrailingZeros(y)), sign);
	}
	int xlen = x.length;
	int[] rmag = new int[xlen + 1];
	long carry = 0;
	long yl = y & LONG_MASK;
	int rstart = rmag.length - 1;
	for (int i = xlen - 1; i &gt;= 0; i--) {
	    long product = (x[i] & LONG_MASK) * yl + carry;
	    rmag[rstart--] = (int) product;
	    carry = product &gt;&gt;&gt; 32;
	}
	if (carry == 0L) {
	    rmag = java.util.Arrays.copyOfRange(rmag, 1, rmag.length);
	} else {
	    rmag[rstart] = (int) carry;
	}
	return new BigInteger(rmag, sign);
    }

    /**
     * Multiplies int arrays x and y to the specified lengths and places
     * the result into z. There will be no leading zeros in the resultant array.
     */
    private static int[] multiplyToLen(int[] x, int xlen, int[] y, int ylen, int[] z) {
	multiplyToLenCheck(x, xlen);
	multiplyToLenCheck(y, ylen);
	return implMultiplyToLen(x, xlen, y, ylen, z);
    }

    /**
     * Returns the input array stripped of any leading zero bytes.
     * Since the source is trusted the copying may be skipped.
     */
    private static int[] trustedStripLeadingZeroInts(int val[]) {
	int vlen = val.length;
	int keep;

	// Find first nonzero byte
	for (keep = 0; keep &lt; vlen && val[keep] == 0; keep++)
	    ;
	return keep == 0 ? val : java.util.Arrays.copyOfRange(val, keep, vlen);
    }

    /**
     * Multiplies two BigIntegers using the Karatsuba multiplication
     * algorithm.  This is a recursive divide-and-conquer algorithm which is
     * more efficient for large numbers than what is commonly called the
     * "grade-school" algorithm used in multiplyToLen.  If the numbers to be
     * multiplied have length n, the "grade-school" algorithm has an
     * asymptotic complexity of O(n^2).  In contrast, the Karatsuba algorithm
     * has complexity of O(n^(log2(3))), or O(n^1.585).  It achieves this
     * increased performance by doing 3 multiplies instead of 4 when
     * evaluating the product.  As it has some overhead, should be used when
     * both numbers are larger than a certain threshold (found
     * experimentally).
     *
     * See:  http://en.wikipedia.org/wiki/Karatsuba_algorithm
     */
    private static BigInteger multiplyKaratsuba(BigInteger x, BigInteger y) {
	int xlen = x.mag.length;
	int ylen = y.mag.length;

	// The number of ints in each half of the number.
	int half = (Math.max(xlen, ylen) + 1) / 2;

	// xl and yl are the lower halves of x and y respectively,
	// xh and yh are the upper halves.
	BigInteger xl = x.getLower(half);
	BigInteger xh = x.getUpper(half);
	BigInteger yl = y.getLower(half);
	BigInteger yh = y.getUpper(half);

	BigInteger p1 = xh.multiply(yh); // p1 = xh*yh
	BigInteger p2 = xl.multiply(yl); // p2 = xl*yl

	// p3=(xh+xl)*(yh+yl)
	BigInteger p3 = xh.add(xl).multiply(yh.add(yl));

	// result = p1 * 2^(32*2*half) + (p3 - p1 - p2) * 2^(32*half) + p2
	BigInteger result = p1.shiftLeft(32 * half).add(p3.subtract(p1).subtract(p2)).shiftLeft(32 * half).add(p2);

	if (x.signum != y.signum) {
	    return result.negate();
	} else {
	    return result;
	}
    }

    /**
     * Multiplies two BigIntegers using a 3-way Toom-Cook multiplication
     * algorithm.  This is a recursive divide-and-conquer algorithm which is
     * more efficient for large numbers than what is commonly called the
     * "grade-school" algorithm used in multiplyToLen.  If the numbers to be
     * multiplied have length n, the "grade-school" algorithm has an
     * asymptotic complexity of O(n^2).  In contrast, 3-way Toom-Cook has a
     * complexity of about O(n^1.465).  It achieves this increased asymptotic
     * performance by breaking each number into three parts and by doing 5
     * multiplies instead of 9 when evaluating the product.  Due to overhead
     * (additions, shifts, and one division) in the Toom-Cook algorithm, it
     * should only be used when both numbers are larger than a certain
     * threshold (found experimentally).  This threshold is generally larger
     * than that for Karatsuba multiplication, so this algorithm is generally
     * only used when numbers become significantly larger.
     *
     * The algorithm used is the "optimal" 3-way Toom-Cook algorithm outlined
     * by Marco Bodrato.
     *
     *  See: http://bodrato.it/toom-cook/
     *       http://bodrato.it/papers/#WAIFI2007
     *
     * "Towards Optimal Toom-Cook Multiplication for Univariate and
     * Multivariate Polynomials in Characteristic 2 and 0." by Marco BODRATO;
     * In C.Carlet and B.Sunar, Eds., "WAIFI'07 proceedings", p. 116-133,
     * LNCS #4547. Springer, Madrid, Spain, June 21-22, 2007.
     *
     */
    private static BigInteger multiplyToomCook3(BigInteger a, BigInteger b) {
	int alen = a.mag.length;
	int blen = b.mag.length;

	int largest = Math.max(alen, blen);

	// k is the size (in ints) of the lower-order slices.
	int k = (largest + 2) / 3; // Equal to ceil(largest/3)

	// r is the size (in ints) of the highest-order slice.
	int r = largest - 2 * k;

	// Obtain slices of the numbers. a2 and b2 are the most significant
	// bits of the numbers a and b, and a0 and b0 the least significant.
	BigInteger a0, a1, a2, b0, b1, b2;
	a2 = a.getToomSlice(k, r, 0, largest);
	a1 = a.getToomSlice(k, r, 1, largest);
	a0 = a.getToomSlice(k, r, 2, largest);
	b2 = b.getToomSlice(k, r, 0, largest);
	b1 = b.getToomSlice(k, r, 1, largest);
	b0 = b.getToomSlice(k, r, 2, largest);

	BigInteger v0, v1, v2, vm1, vinf, t1, t2, tm1, da1, db1;

	v0 = a0.multiply(b0);
	da1 = a2.add(a0);
	db1 = b2.add(b0);
	vm1 = da1.subtract(a1).multiply(db1.subtract(b1));
	da1 = da1.add(a1);
	db1 = db1.add(b1);
	v1 = da1.multiply(db1);
	v2 = da1.add(a2).shiftLeft(1).subtract(a0).multiply(db1.add(b2).shiftLeft(1).subtract(b0));
	vinf = a2.multiply(b2);

	// The algorithm requires two divisions by 2 and one by 3.
	// All divisions are known to be exact, that is, they do not produce
	// remainders, and all results are positive.  The divisions by 2 are
	// implemented as right shifts which are relatively efficient, leaving
	// only an exact division by 3, which is done by a specialized
	// linear-time algorithm.
	t2 = v2.subtract(vm1).exactDivideBy3();
	tm1 = v1.subtract(vm1).shiftRight(1);
	t1 = v1.subtract(v0);
	t2 = t2.subtract(t1).shiftRight(1);
	t1 = t1.subtract(tm1).subtract(vinf);
	t2 = t2.subtract(vinf.shiftLeft(1));
	tm1 = tm1.subtract(t2);

	// Number of bits to shift left.
	int ss = k * 32;

	BigInteger result = vinf.shiftLeft(ss).add(t2).shiftLeft(ss).add(t1).shiftLeft(ss).add(tm1).shiftLeft(ss)
		.add(v0);

	if (a.signum != b.signum) {
	    return result.negate();
	} else {
	    return result;
	}
    }

    /**
     * Squares the contents of the int array x. The result is placed into the
     * int array z.  The contents of x are not changed.
     */
    private static final int[] squareToLen(int[] x, int len, int[] z) {
	int zlen = len &lt;&lt; 1;
	if (z == null || z.length &lt; zlen)
	    z = new int[zlen];

	// Execute checks before calling intrinsified method.
	implSquareToLenChecks(x, len, z, zlen);
	return implSquareToLen(x, len, z, zlen);
    }

    /**
     * Squares a BigInteger using the Karatsuba squaring algorithm.  It should
     * be used when both numbers are larger than a certain threshold (found
     * experimentally).  It is a recursive divide-and-conquer algorithm that
     * has better asymptotic performance than the algorithm used in
     * squareToLen.
     */
    private BigInteger squareKaratsuba() {
	int half = (mag.length + 1) / 2;

	BigInteger xl = getLower(half);
	BigInteger xh = getUpper(half);

	BigInteger xhs = xh.square(); // xhs = xh^2
	BigInteger xls = xl.square(); // xls = xl^2

	// xh^2 &lt;&lt; 64  +  (((xl+xh)^2 - (xh^2 + xl^2)) &lt;&lt; 32) + xl^2
	return xhs.shiftLeft(half * 32).add(xl.add(xh).square().subtract(xhs.add(xls))).shiftLeft(half * 32).add(xls);
    }

    /**
     * Squares a BigInteger using the 3-way Toom-Cook squaring algorithm.  It
     * should be used when both numbers are larger than a certain threshold
     * (found experimentally).  It is a recursive divide-and-conquer algorithm
     * that has better asymptotic performance than the algorithm used in
     * squareToLen or squareKaratsuba.
     */
    private BigInteger squareToomCook3() {
	int len = mag.length;

	// k is the size (in ints) of the lower-order slices.
	int k = (len + 2) / 3; // Equal to ceil(largest/3)

	// r is the size (in ints) of the highest-order slice.
	int r = len - 2 * k;

	// Obtain slices of the numbers. a2 is the most significant
	// bits of the number, and a0 the least significant.
	BigInteger a0, a1, a2;
	a2 = getToomSlice(k, r, 0, len);
	a1 = getToomSlice(k, r, 1, len);
	a0 = getToomSlice(k, r, 2, len);
	BigInteger v0, v1, v2, vm1, vinf, t1, t2, tm1, da1;

	v0 = a0.square();
	da1 = a2.add(a0);
	vm1 = da1.subtract(a1).square();
	da1 = da1.add(a1);
	v1 = da1.square();
	vinf = a2.square();
	v2 = da1.add(a2).shiftLeft(1).subtract(a0).square();

	// The algorithm requires two divisions by 2 and one by 3.
	// All divisions are known to be exact, that is, they do not produce
	// remainders, and all results are positive.  The divisions by 2 are
	// implemented as right shifts which are relatively efficient, leaving
	// only a division by 3.
	// The division by 3 is done by an optimized algorithm for this case.
	t2 = v2.subtract(vm1).exactDivideBy3();
	tm1 = v1.subtract(vm1).shiftRight(1);
	t1 = v1.subtract(v0);
	t2 = t2.subtract(t1).shiftRight(1);
	t1 = t1.subtract(tm1).subtract(vinf);
	t2 = t2.subtract(vinf.shiftLeft(1));
	tm1 = tm1.subtract(t2);

	// Number of bits to shift left.
	int ss = k * 32;

	return vinf.shiftLeft(ss).add(t2).shiftLeft(ss).add(t1).shiftLeft(ss).add(tm1).shiftLeft(ss).add(v0);
    }

    private int signInt() {
	return signum &lt; 0 ? -1 : 0;
    }

    /**
    * Returns the index of the int that contains the first nonzero int in the
    * little-endian binary representation of the magnitude (int 0 is the
    * least significant). If the magnitude is zero, return value is undefined.
    *
    * &lt;p&gt;Note: never used for a BigInteger with a magnitude of zero.
    * @see #getInt.
    */
    private int firstNonzeroIntNum() {
	int fn = firstNonzeroIntNumPlusTwo - 2;
	if (fn == -2) { // firstNonzeroIntNum not initialized yet
	    // Search for the first nonzero int
	    int i;
	    int mlen = mag.length;
	    for (i = mlen - 1; i &gt;= 0 && mag[i] == 0; i--)
		;
	    fn = mlen - i - 1;
	    firstNonzeroIntNumPlusTwo = fn + 2; // offset by two to initialize
	}
	return fn;
    }

    int[] javaIncrement(int[] val) {
	int lastSum = 0;
	for (int i = val.length - 1; i &gt;= 0 && lastSum == 0; i--)
	    lastSum = (val[i] += 1);
	if (lastSum == 0) {
	    val = new int[val.length + 1];
	    val[0] = 1;
	}
	return val;
    }

    /**
     * Throws an {@code ArithmeticException} if the {@code BigInteger} would be
     * out of the supported range.
     *
     * @throws ArithmeticException if {@code this} exceeds the supported range.
     */
    private void checkRange() {
	if (mag.length &gt; MAX_MAG_LENGTH || mag.length == MAX_MAG_LENGTH && mag[0] &lt; 0) {
	    reportOverflow();
	}
    }

    private static void multiplyToLenCheck(int[] array, int length) {
	if (length &lt;= 0) {
	    return; // not an error because multiplyToLen won't execute if len &lt;= 0
	}

	Objects.requireNonNull(array);

	if (length &gt; array.length) {
	    throw new ArrayIndexOutOfBoundsException(length - 1);
	}
    }

    @HotSpotIntrinsicCandidate
    private static int[] implMultiplyToLen(int[] x, int xlen, int[] y, int ylen, int[] z) {
	int xstart = xlen - 1;
	int ystart = ylen - 1;

	if (z == null || z.length &lt; (xlen + ylen))
	    z = new int[xlen + ylen];

	long carry = 0;
	for (int j = ystart, k = ystart + 1 + xstart; j &gt;= 0; j--, k--) {
	    long product = (y[j] & LONG_MASK) * (x[xstart] & LONG_MASK) + carry;
	    z[k] = (int) product;
	    carry = product &gt;&gt;&gt; 32;
	}
	z[xstart] = (int) carry;

	for (int i = xstart - 1; i &gt;= 0; i--) {
	    carry = 0;
	    for (int j = ystart, k = ystart + 1 + i; j &gt;= 0; j--, k--) {
		long product = (y[j] & LONG_MASK) * (x[i] & LONG_MASK) + (z[k] & LONG_MASK) + carry;
		z[k] = (int) product;
		carry = product &gt;&gt;&gt; 32;
	    }
	    z[i] = (int) carry;
	}
	return z;
    }

    /**
     * Returns a new BigInteger representing n lower ints of the number.
     * This is used by Karatsuba multiplication and Karatsuba squaring.
     */
    private BigInteger getLower(int n) {
	int len = mag.length;

	if (len &lt;= n) {
	    return abs();
	}

	int lowerInts[] = new int[n];
	System.arraycopy(mag, len - n, lowerInts, 0, n);

	return new BigInteger(trustedStripLeadingZeroInts(lowerInts), 1);
    }

    /**
     * Returns a new BigInteger representing mag.length-n upper
     * ints of the number.  This is used by Karatsuba multiplication and
     * Karatsuba squaring.
     */
    private BigInteger getUpper(int n) {
	int len = mag.length;

	if (len &lt;= n) {
	    return ZERO;
	}

	int upperLen = len - n;
	int upperInts[] = new int[upperLen];
	System.arraycopy(mag, 0, upperInts, 0, upperLen);

	return new BigInteger(trustedStripLeadingZeroInts(upperInts), 1);
    }

    /**
     * Returns a BigInteger whose value is {@code (this + val)}.
     *
     * @param  val value to be added to this BigInteger.
     * @return {@code this + val}
     */
    public BigInteger add(BigInteger val) {
	if (val.signum == 0)
	    return this;
	if (signum == 0)
	    return val;
	if (val.signum == signum)
	    return new BigInteger(add(mag, val.mag), signum);

	int cmp = compareMagnitude(val);
	if (cmp == 0)
	    return ZERO;
	int[] resultMag = (cmp &gt; 0 ? subtract(mag, val.mag) : subtract(val.mag, mag));
	resultMag = trustedStripLeadingZeroInts(resultMag);

	return new BigInteger(resultMag, cmp == signum ? 1 : -1);
    }

    /**
     * Returns a BigInteger whose value is {@code (this - val)}.
     *
     * @param  val value to be subtracted from this BigInteger.
     * @return {@code this - val}
     */
    public BigInteger subtract(BigInteger val) {
	if (val.signum == 0)
	    return this;
	if (signum == 0)
	    return val.negate();
	if (val.signum != signum)
	    return new BigInteger(add(mag, val.mag), signum);

	int cmp = compareMagnitude(val);
	if (cmp == 0)
	    return ZERO;
	int[] resultMag = (cmp &gt; 0 ? subtract(mag, val.mag) : subtract(val.mag, mag));
	resultMag = trustedStripLeadingZeroInts(resultMag);
	return new BigInteger(resultMag, cmp == signum ? 1 : -1);
    }

    /**
     * Returns a slice of a BigInteger for use in Toom-Cook multiplication.
     *
     * @param lowerSize The size of the lower-order bit slices.
     * @param upperSize The size of the higher-order bit slices.
     * @param slice The index of which slice is requested, which must be a
     * number from 0 to size-1. Slice 0 is the highest-order bits, and slice
     * size-1 are the lowest-order bits. Slice 0 may be of different size than
     * the other slices.
     * @param fullsize The size of the larger integer array, used to align
     * slices to the appropriate position when multiplying different-sized
     * numbers.
     */
    private BigInteger getToomSlice(int lowerSize, int upperSize, int slice, int fullsize) {
	int start, end, sliceSize, len, offset;

	len = mag.length;
	offset = fullsize - len;

	if (slice == 0) {
	    start = 0 - offset;
	    end = upperSize - 1 - offset;
	} else {
	    start = upperSize + (slice - 1) * lowerSize - offset;
	    end = start + lowerSize - 1;
	}

	if (start &lt; 0) {
	    start = 0;
	}
	if (end &lt; 0) {
	    return ZERO;
	}

	sliceSize = (end - start) + 1;

	if (sliceSize &lt;= 0) {
	    return ZERO;
	}

	// While performing Toom-Cook, all slices are positive and
	// the sign is adjusted when the final number is composed.
	if (start == 0 && sliceSize &gt;= len) {
	    return this.abs();
	}

	int intSlice[] = new int[sliceSize];
	System.arraycopy(mag, start, intSlice, 0, sliceSize);

	return new BigInteger(trustedStripLeadingZeroInts(intSlice), 1);
    }

    /**
     * Does an exact division (that is, the remainder is known to be zero)
     * of the specified number by 3.  This is used in Toom-Cook
     * multiplication.  This is an efficient algorithm that runs in linear
     * time.  If the argument is not exactly divisible by 3, results are
     * undefined.  Note that this is expected to be called with positive
     * arguments only.
     */
    private BigInteger exactDivideBy3() {
	int len = mag.length;
	int[] result = new int[len];
	long x, w, q, borrow;
	borrow = 0L;
	for (int i = len - 1; i &gt;= 0; i--) {
	    x = (mag[i] & LONG_MASK);
	    w = x - borrow;
	    if (borrow &gt; x) { // Did we make the number go negative?
		borrow = 1L;
	    } else {
		borrow = 0L;
	    }

	    // 0xAAAAAAAB is the modular inverse of 3 (mod 2^32).  Thus,
	    // the effect of this is to divide by 3 (mod 2^32).
	    // This is much faster than division on most architectures.
	    q = (w * 0xAAAAAAABL) & LONG_MASK;
	    result[i] = (int) q;

	    // Now check the borrow. The second check can of course be
	    // eliminated if the first fails.
	    if (q &gt;= 0x55555556L) {
		borrow++;
		if (q &gt;= 0xAAAAAAABL)
		    borrow++;
	    }
	}
	result = trustedStripLeadingZeroInts(result);
	return new BigInteger(result, signum);
    }

    /**
      * Parameters validation.
      */
    private static void implSquareToLenChecks(int[] x, int len, int[] z, int zlen) throws RuntimeException {
	if (len &lt; 1) {
	    throw new IllegalArgumentException("invalid input length: " + len);
	}
	if (len &gt; x.length) {
	    throw new IllegalArgumentException("input length out of bound: " + len + " &gt; " + x.length);
	}
	if (len * 2 &gt; z.length) {
	    throw new IllegalArgumentException("input length out of bound: " + (len * 2) + " &gt; " + z.length);
	}
	if (zlen &lt; 1) {
	    throw new IllegalArgumentException("invalid input length: " + zlen);
	}
	if (zlen &gt; z.length) {
	    throw new IllegalArgumentException("input length out of bound: " + len + " &gt; " + z.length);
	}
    }

    /**
      * Java Runtime may use intrinsic for this method.
      */
    @HotSpotIntrinsicCandidate
    private static final int[] implSquareToLen(int[] x, int len, int[] z, int zlen) {
	/*
	 * The algorithm used here is adapted from Colin Plumb's C library.
	 * Technique: Consider the partial products in the multiplication
	 * of "abcde" by itself:
	 *
	 *               a  b  c  d  e
	 *            *  a  b  c  d  e
	 *          ==================
	 *              ae be ce de ee
	 *           ad bd cd dd de
	 *        ac bc cc cd ce
	 *     ab bb bc bd be
	 *  aa ab ac ad ae
	 *
	 * Note that everything above the main diagonal:
	 *              ae be ce de = (abcd) * e
	 *           ad bd cd       = (abc) * d
	 *        ac bc             = (ab) * c
	 *     ab                   = (a) * b
	 *
	 * is a copy of everything below the main diagonal:
	 *                       de
	 *                 cd ce
	 *           bc bd be
	 *     ab ac ad ae
	 *
	 * Thus, the sum is 2 * (off the diagonal) + diagonal.
	 *
	 * This is accumulated beginning with the diagonal (which
	 * consist of the squares of the digits of the input), which is then
	 * divided by two, the off-diagonal added, and multiplied by two
	 * again.  The low bit is simply a copy of the low bit of the
	 * input, so it doesn't need special care.
	 */

	// Store the squares, right shifted one bit (i.e., divided by 2)
	int lastProductLowWord = 0;
	for (int j = 0, i = 0; j &lt; len; j++) {
	    long piece = (x[j] & LONG_MASK);
	    long product = piece * piece;
	    z[i++] = (lastProductLowWord &lt;&lt; 31) | (int) (product &gt;&gt;&gt; 33);
	    z[i++] = (int) (product &gt;&gt;&gt; 1);
	    lastProductLowWord = (int) product;
	}

	// Add in off-diagonal sums
	for (int i = len, offset = 1; i &gt; 0; i--, offset += 2) {
	    int t = x[i - 1];
	    t = mulAdd(z, x, offset, i - 1, t);
	    addOne(z, offset - 1, i, t);
	}

	// Shift back up and set low bit
	primitiveLeftShift(z, zlen, 1);
	z[zlen - 1] |= x[len - 1] & 1;

	return z;
    }

    /**
     * Adds the contents of the int arrays x and y. This method allocates
     * a new int array to hold the answer and returns a reference to that
     * array.
     */
    private static int[] add(int[] x, int[] y) {
	// If x is shorter, swap the two arrays
	if (x.length &lt; y.length) {
	    int[] tmp = x;
	    x = y;
	    y = tmp;
	}

	int xIndex = x.length;
	int yIndex = y.length;
	int result[] = new int[xIndex];
	long sum = 0;
	if (yIndex == 1) {
	    sum = (x[--xIndex] & LONG_MASK) + (y[0] & LONG_MASK);
	    result[xIndex] = (int) sum;
	} else {
	    // Add common parts of both numbers
	    while (yIndex &gt; 0) {
		sum = (x[--xIndex] & LONG_MASK) + (y[--yIndex] & LONG_MASK) + (sum &gt;&gt;&gt; 32);
		result[xIndex] = (int) sum;
	    }
	}
	// Copy remainder of longer number while carry propagation is required
	boolean carry = (sum &gt;&gt;&gt; 32 != 0);
	while (xIndex &gt; 0 && carry)
	    carry = ((result[--xIndex] = x[xIndex] + 1) == 0);

	// Copy remainder of longer number
	while (xIndex &gt; 0)
	    result[--xIndex] = x[xIndex];

	// Grow result if necessary
	if (carry) {
	    int bigger[] = new int[result.length + 1];
	    System.arraycopy(result, 0, bigger, 1, result.length);
	    bigger[0] = 0x01;
	    return bigger;
	}
	return result;
    }

    /**
     * Compares the magnitude array of this BigInteger with the specified
     * BigInteger's. This is the version of compareTo ignoring sign.
     *
     * @param val BigInteger whose magnitude array to be compared.
     * @return -1, 0 or 1 as this magnitude array is less than, equal to or
     *         greater than the magnitude aray for the specified BigInteger's.
     */
    final int compareMagnitude(BigInteger val) {
	int[] m1 = mag;
	int len1 = m1.length;
	int[] m2 = val.mag;
	int len2 = m2.length;
	if (len1 &lt; len2)
	    return -1;
	if (len1 &gt; len2)
	    return 1;
	for (int i = 0; i &lt; len1; i++) {
	    int a = m1[i];
	    int b = m2[i];
	    if (a != b)
		return ((a & LONG_MASK) &lt; (b & LONG_MASK)) ? -1 : 1;
	}
	return 0;
    }

    /**
     * Subtracts the contents of the second int arrays (little) from the
     * first (big).  The first int array (big) must represent a larger number
     * than the second.  This method allocates the space necessary to hold the
     * answer.
     */
    private static int[] subtract(int[] big, int[] little) {
	int bigIndex = big.length;
	int result[] = new int[bigIndex];
	int littleIndex = little.length;
	long difference = 0;

	// Subtract common parts of both numbers
	while (littleIndex &gt; 0) {
	    difference = (big[--bigIndex] & LONG_MASK) - (little[--littleIndex] & LONG_MASK) + (difference &gt;&gt; 32);
	    result[bigIndex] = (int) difference;
	}

	// Subtract remainder of longer number while borrow propagates
	boolean borrow = (difference &gt;&gt; 32 != 0);
	while (bigIndex &gt; 0 && borrow)
	    borrow = ((result[--bigIndex] = big[bigIndex] - 1) == -1);

	// Copy remainder of longer number
	while (bigIndex &gt; 0)
	    result[--bigIndex] = big[bigIndex];

	return result;
    }

    /**
     * Multiply an array by one word k and add to result, return the carry
     */
    static int mulAdd(int[] out, int[] in, int offset, int len, int k) {
	implMulAddCheck(out, in, offset, len, k);
	return implMulAdd(out, in, offset, len, k);
    }

    /**
     * Add one word to the number a mlen words into a. Return the resulting
     * carry.
     */
    static int addOne(int[] a, int offset, int mlen, int carry) {
	offset = a.length - 1 - mlen - offset;
	long t = (a[offset] & LONG_MASK) + (carry & LONG_MASK);

	a[offset] = (int) t;
	if ((t &gt;&gt;&gt; 32) == 0)
	    return 0;
	while (--mlen &gt;= 0) {
	    if (--offset &lt; 0) { // Carry out of number
		return 1;
	    } else {
		a[offset]++;
		if (a[offset] != 0)
		    return 0;
	    }
	}
	return 1;
    }

    static void primitiveLeftShift(int[] a, int len, int n) {
	if (len == 0 || n == 0)
	    return;

	int n2 = 32 - n;
	for (int i = 0, c = a[i], m = i + len - 1; i &lt; m; i++) {
	    int b = c;
	    c = a[i + 1];
	    a[i] = (b &lt;&lt; n) | (c &gt;&gt;&gt; n2);
	}
	a[len - 1] &lt;&lt;= n;
    }

    /**
     * Parameters validation.
     */
    private static void implMulAddCheck(int[] out, int[] in, int offset, int len, int k) {
	if (len &gt; in.length) {
	    throw new IllegalArgumentException("input length is out of bound: " + len + " &gt; " + in.length);
	}
	if (offset &lt; 0) {
	    throw new IllegalArgumentException("input offset is invalid: " + offset);
	}
	if (offset &gt; (out.length - 1)) {
	    throw new IllegalArgumentException("input offset is out of bound: " + offset + " &gt; " + (out.length - 1));
	}
	if (len &gt; (out.length - offset)) {
	    throw new IllegalArgumentException("input len is out of bound: " + len + " &gt; " + (out.length - offset));
	}
    }

    /**
     * Java Runtime may use intrinsic for this method.
     */
    @HotSpotIntrinsicCandidate
    private static int implMulAdd(int[] out, int[] in, int offset, int len, int k) {
	long kLong = k & LONG_MASK;
	long carry = 0;

	offset = out.length - offset - 1;
	for (int j = len - 1; j &gt;= 0; j--) {
	    long product = (in[j] & LONG_MASK) * kLong + (out[offset] & LONG_MASK) + carry;
	    out[offset--] = (int) product;
	    carry = product &gt;&gt;&gt; 32;
	}
	return (int) carry;
    }

}

