import static com.google.common.math.MathPreconditions.checkPositive;
import static com.google.common.math.MathPreconditions.checkRoundingUnnecessary;
import static java.math.RoundingMode.CEILING;
import static java.math.RoundingMode.FLOOR;
import static java.math.RoundingMode.HALF_EVEN;
import java.math.BigInteger;
import java.math.RoundingMode;

class BigIntegerMath {
    /**
    * Returns the base-10 logarithm of {@code x}, rounded according to the specified rounding mode.
    *
    * @throws IllegalArgumentException if {@code x &lt;= 0}
    * @throws ArithmeticException if {@code mode} is {@link RoundingMode#UNNECESSARY} and {@code x}
    *     is not a power of ten
    */
    @GwtIncompatible // TODO
    @SuppressWarnings("fallthrough")
    public static int log10(BigInteger x, RoundingMode mode) {
	checkPositive("x", x);
	if (fitsInLong(x)) {
	    return LongMath.log10(x.longValue(), mode);
	}

	int approxLog10 = (int) (log2(x, FLOOR) * LN_2 / LN_10);
	BigInteger approxPow = BigInteger.TEN.pow(approxLog10);
	int approxCmp = approxPow.compareTo(x);

	/*
	 * We adjust approxLog10 and approxPow until they're equal to floor(log10(x)) and
	 * 10^floor(log10(x)).
	 */

	if (approxCmp &gt; 0) {
	    /*
	     * The code is written so that even completely incorrect approximations will still yield the
	     * correct answer eventually, but in practice this branch should almost never be entered, and
	     * even then the loop should not run more than once.
	     */
	    do {
		approxLog10--;
		approxPow = approxPow.divide(BigInteger.TEN);
		approxCmp = approxPow.compareTo(x);
	    } while (approxCmp &gt; 0);
	} else {
	    BigInteger nextPow = BigInteger.TEN.multiply(approxPow);
	    int nextCmp = nextPow.compareTo(x);
	    while (nextCmp &lt;= 0) {
		approxLog10++;
		approxPow = nextPow;
		approxCmp = nextCmp;
		nextPow = BigInteger.TEN.multiply(approxPow);
		nextCmp = nextPow.compareTo(x);
	    }
	}

	int floorLog = approxLog10;
	BigInteger floorPow = approxPow;
	int floorCmp = approxCmp;

	switch (mode) {
	case UNNECESSARY:
	    checkRoundingUnnecessary(floorCmp == 0);
	    // fall through
	case FLOOR:
	case DOWN:
	    return floorLog;

	case CEILING:
	case UP:
	    return floorPow.equals(x) ? floorLog : floorLog + 1;

	case HALF_DOWN:
	case HALF_UP:
	case HALF_EVEN:
	    // Since sqrt(10) is irrational, log10(x) - floorLog can never be exactly 0.5
	    BigInteger x2 = x.pow(2);
	    BigInteger halfPowerSquared = floorPow.pow(2).multiply(BigInteger.TEN);
	    return (x2.compareTo(halfPowerSquared) &lt;= 0) ? floorLog : floorLog + 1;
	default:
	    throw new AssertionError();
	}
    }

    private static final double LN_2 = Math.log(2);
    private static final double LN_10 = Math.log(10);
    @VisibleForTesting
    static final int SQRT2_PRECOMPUTE_THRESHOLD = 256;
    @VisibleForTesting
    static final BigInteger SQRT2_PRECOMPUTED_BITS = new BigInteger(
	    "16a09e667f3bcc908b2fb1366ea957d3e3adec17512775099da2f590b0667322a", 16);

    @GwtIncompatible // TODO
    static boolean fitsInLong(BigInteger x) {
	return x.bitLength() &lt;= Long.SIZE - 1;
    }

    /**
    * Returns the base-2 logarithm of {@code x}, rounded according to the specified rounding mode.
    *
    * @throws IllegalArgumentException if {@code x &lt;= 0}
    * @throws ArithmeticException if {@code mode} is {@link RoundingMode#UNNECESSARY} and {@code x}
    *     is not a power of two
    */
    @SuppressWarnings("fallthrough")
    // TODO(kevinb): remove after this warning is disabled globally
    public static int log2(BigInteger x, RoundingMode mode) {
	checkPositive("x", checkNotNull(x));
	int logFloor = x.bitLength() - 1;
	switch (mode) {
	case UNNECESSARY:
	    checkRoundingUnnecessary(isPowerOfTwo(x)); // fall through
	case DOWN:
	case FLOOR:
	    return logFloor;

	case UP:
	case CEILING:
	    return isPowerOfTwo(x) ? logFloor : logFloor + 1;

	case HALF_DOWN:
	case HALF_UP:
	case HALF_EVEN:
	    if (logFloor &lt; SQRT2_PRECOMPUTE_THRESHOLD) {
		BigInteger halfPower = SQRT2_PRECOMPUTED_BITS.shiftRight(SQRT2_PRECOMPUTE_THRESHOLD - logFloor);
		if (x.compareTo(halfPower) &lt;= 0) {
		    return logFloor;
		} else {
		    return logFloor + 1;
		}
	    }
	    // Since sqrt(2) is irrational, log2(x) - logFloor cannot be exactly 0.5
	    //
	    // To determine which side of logFloor.5 the logarithm is,
	    // we compare x^2 to 2^(2 * logFloor + 1).
	    BigInteger x2 = x.pow(2);
	    int logX2Floor = x2.bitLength() - 1;
	    return (logX2Floor &lt; 2 * logFloor + 1) ? logFloor : logFloor + 1;

	default:
	    throw new AssertionError();
	}
    }

    /** Returns {@code true} if {@code x} represents a power of two. */
    public static boolean isPowerOfTwo(BigInteger x) {
	checkNotNull(x);
	return x.signum() &gt; 0 && x.getLowestSetBit() == x.bitLength() - 1;
    }

}

