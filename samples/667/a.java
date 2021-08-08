import org.apache.commons.math4.exception.NullArgumentException;

class MathUtils {
    /**
     * Checks that an object is not null.
     *
     * @param o Object to be checked.
     * @throws NullArgumentException if {@code o} is {@code null}.
     */
    public static void checkNotNull(Object o) throws NullArgumentException {
	if (o == null) {
	    throw new NullArgumentException();
	}
    }

}

