import org.apache.commons.math4.exception.NotFiniteNumberException;
import org.apache.commons.math4.exception.util.LocalizedFormats;

class MathUtils {
    /**
     * Check that all the elements are real numbers.
     *
     * @param val Arguments.
     * @throws NotFiniteNumberException if any values of the array is not a
     * finite real number.
     */
    public static void checkFinite(final double[] val) throws NotFiniteNumberException {
	for (int i = 0; i &lt; val.length; i++) {
	    final double x = val[i];
	    if (Double.isInfinite(x) || Double.isNaN(x)) {
		throw new NotFiniteNumberException(LocalizedFormats.ARRAY_ELEMENT, x, i);
	    }
	}
    }

}

