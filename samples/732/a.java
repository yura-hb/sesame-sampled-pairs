import java.util.Random;

class RandomUtils {
    /**
     * &lt;p&gt;
     * Creates an array of random bytes.
     * &lt;/p&gt;
     *
     * @param count
     *            the size of the returned array
     * @return the random byte array
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public static byte[] nextBytes(final int count) {
	Validate.isTrue(count &gt;= 0, "Count cannot be negative.");

	final byte[] result = new byte[count];
	RANDOM.nextBytes(result);
	return result;
    }

    /**
     * Random object used by random method. This has to be not local to the
     * random method so as to not return the same value in the same millisecond.
     */
    private static final Random RANDOM = new Random();

}

