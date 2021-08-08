import org.apache.commons.math4.exception.NotFiniteNumberException;

class MathUtils {
    /**
     * Check that the argument is a real number.
     *
     * @param x Argument.
     * @throws NotFiniteNumberException if {@code x} is not a
     * finite real number.
     */
    public static void checkFinite(final double x) throws NotFiniteNumberException {
	if (Double.isInfinite(x) || Double.isNaN(x)) {
	    throw new NotFiniteNumberException(x);
	}
    }

}

