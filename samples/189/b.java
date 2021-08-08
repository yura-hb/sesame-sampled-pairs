import java.util.Objects;

class Asserts {
    /**
     * Asserts that {@code lhs} is not equal to {@code rhs}.
     *
     * @param lhs The left hand side of the comparison.
     * @param rhs The right hand side of the comparison.
     * @param msg A description of the assumption; {@code null} for a default message.
     * @throws RuntimeException if the assertion is not true.
     */
    public static void assertNotEquals(Object lhs, Object rhs, String msg) {
	if ((lhs == rhs) || (lhs != null && lhs.equals(rhs))) {
	    msg = Objects.toString(msg, "assertNotEquals") + ": expected " + Objects.toString(lhs) + " to not equal "
		    + Objects.toString(rhs);
	    fail(msg);
	}
    }

    /**
     * Fail reports a failure with a message.
     * @param message for the failure
     * @throws RuntimeException always
     */
    public static void fail(String message) {
	throw new RuntimeException(message);
    }

}

