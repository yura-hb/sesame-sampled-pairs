import static com.google.common.math.MathPreconditions.checkRoundingUnnecessary;
import static java.math.RoundingMode.HALF_EVEN;
import static java.math.RoundingMode.HALF_UP;
import java.math.RoundingMode;

class IntMath {
    /**
    * Returns the square root of {@code x}, rounded with the specified rounding mode.
    *
    * @throws IllegalArgumentException if {@code x &lt; 0}
    * @throws ArithmeticException if {@code mode} is {@link RoundingMode#UNNECESSARY} and {@code
    *     sqrt(x)} is not an integer
    */
    @GwtIncompatible // need BigIntegerMath to adequately test
    @SuppressWarnings("fallthrough")
    public static int sqrt(int x, RoundingMode mode) {
	checkNonNegative("x", x);
	int sqrtFloor = sqrtFloor(x);
	switch (mode) {
	case UNNECESSARY:
	    checkRoundingUnnecessary(sqrtFloor * sqrtFloor == x); // fall through
	case FLOOR:
	case DOWN:
	    return sqrtFloor;
	case CEILING:
	case UP:
	    return sqrtFloor + lessThanBranchFree(sqrtFloor * sqrtFloor, x);
	case HALF_DOWN:
	case HALF_UP:
	case HALF_EVEN:
	    int halfSquare = sqrtFloor * sqrtFloor + sqrtFloor;
	    /*
	     * We wish to test whether or not x &lt;= (sqrtFloor + 0.5)^2 = halfSquare + 0.25. Since both x
	     * and halfSquare are integers, this is equivalent to testing whether or not x &lt;=
	     * halfSquare. (We have to deal with overflow, though.)
	     *
	     * If we treat halfSquare as an unsigned int, we know that
	     *            sqrtFloor^2 &lt;= x &lt; (sqrtFloor + 1)^2
	     * halfSquare - sqrtFloor &lt;= x &lt; halfSquare + sqrtFloor + 1
	     * so |x - halfSquare| &lt;= sqrtFloor.  Therefore, it's safe to treat x - halfSquare as a
	     * signed int, so lessThanBranchFree is safe for use.
	     */
	    return sqrtFloor + lessThanBranchFree(halfSquare, x);
	default:
	    throw new AssertionError();
	}
    }

    private static int sqrtFloor(int x) {
	// There is no loss of precision in converting an int to a double, according to
	// http://java.sun.com/docs/books/jls/third_edition/html/conversions.html#5.1.2
	return (int) Math.sqrt(x);
    }

    /**
    * Returns 1 if {@code x &lt; y} as unsigned integers, and 0 otherwise. Assumes that x - y fits into
    * a signed int. The implementation is branch-free, and benchmarks suggest it is measurably (if
    * narrowly) faster than the straightforward ternary expression.
    */
    @VisibleForTesting
    static int lessThanBranchFree(int x, int y) {
	// The double negation is optimized away by normal Java, but is necessary for GWT
	// to make sure bit twiddling works as expected.
	return ~~(x - y) &gt;&gt;&gt; (Integer.SIZE - 1);
    }

}

