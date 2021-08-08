import org.apache.commons.math4.exception.MathArithmeticException;
import org.apache.commons.math4.exception.util.LocalizedFormats;

class FastMath {
    /** Increment a number, detecting overflows.
     * @param n number to increment
     * @return n+1 if no overflows occur
     * @exception MathArithmeticException if an overflow occurs
     * @since 3.4
     */
    public static long incrementExact(final long n) throws MathArithmeticException {

	if (n == Long.MAX_VALUE) {
	    throw new MathArithmeticException(LocalizedFormats.OVERFLOW_IN_ADDITION, n, 1);
	}

	return n + 1;

    }

}

