import org.apache.commons.math4.exception.NumberIsTooLargeException;
import org.apache.commons.math4.exception.util.LocalizedFormats;

class RandomUtils {
    class DataGenerator {
	/**
	 * Generates a uniformly distributed random long integer between {@code lower}
	 * and {@code upper} (endpoints included).
	 *
	 * @param lower Lower bound for generated long integer.
	 * @param upper Upper bound for generated long integer.
	 * @return a random long integer greater than or equal to {@code lower}
	 * and less than or equal to {@code upper}
	 * @throws NumberIsTooLargeException if {@code lower &gt;= upper}
	 */
	public long nextLong(final long lower, final long upper) {
	    if (lower &gt;= upper) {
		throw new NumberIsTooLargeException(LocalizedFormats.LOWER_BOUND_NOT_BELOW_UPPER_BOUND, lower, upper,
			false);
	    }
	    final long max = (upper - lower) + 1;
	    if (max &lt;= 0) {
		// Range is too wide to fit in a positive long (larger than 2^63);
		// as it covers more than half the long range, we use directly a
		// simple rejection method.
		while (true) {
		    final long r = rng.nextLong();
		    if (r &gt;= lower && r &lt;= upper) {
			return r;
		    }
		}
	    } else if (max &lt; Integer.MAX_VALUE) {
		// We can shift the range and generate directly a positive int.
		return lower + rng.nextInt((int) max);
	    } else {
		// We can shift the range and generate directly a positive long.
		return lower + rng.nextLong(max);
	    }
	}

	/** Underlying RNG. */
	private final UniformRandomProvider rng;

    }

}

