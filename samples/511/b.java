import java.util.Arrays;

class BigInteger extends Number implements Comparable&lt;BigInteger&gt; {
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
     * The signum of this BigInteger: -1 for negative, 0 for zero, or
     * 1 for positive.  Note that the BigInteger zero &lt;em&gt;must&lt;/em&gt; have
     * a signum of 0.  This is necessary to ensures that there is exactly one
     * representation for each BigInteger value.
     */
    final int signum;
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
     * The BigInteger constant zero.
     *
     * @since   1.2
     */
    public static final BigInteger ZERO = new BigInteger(new int[0], 0);
    /**
     * This mask is used to obtain the value of an int as if it were unsigned.
     */
    static final long LONG_MASK = 0xffffffffL;
    /**
     * This constant limits {@code mag.length} of BigIntegers to the supported
     * range.
     */
    private static final int MAX_MAG_LENGTH = Integer.MAX_VALUE / Integer.SIZE + 1;

    /**
     * Returns a BigInteger whose value is {@code (-this)}.
     *
     * @return {@code -this}
     */
    public BigInteger negate() {
	return new BigInteger(this.mag, -this.signum);
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

    private static void reportOverflow() {
	throw new ArithmeticException("BigInteger would overflow supported range");
    }

}

