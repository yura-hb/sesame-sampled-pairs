import java.util.Arrays;

class KrbAsReqBuilder {
    /**
     * Destroys the object and clears keys and password info.
     */
    public void destroy() {
	state = State.DESTROYED;
	if (password != null) {
	    Arrays.fill(password, (char) 0);
	}
    }

    private State state;
    private final char[] password;

}

