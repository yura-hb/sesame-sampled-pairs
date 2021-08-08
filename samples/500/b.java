class Krb5Context implements GSSContextSpi {
    /**
     * Requests that the deleg policy be respected.
     */
    public final void requestDelegPolicy(boolean value) {
	if (state == STATE_NEW && isInitiator())
	    delegPolicyState = value;
    }

    private int state = STATE_NEW;
    private static final int STATE_NEW = 1;
    private boolean delegPolicyState = false;
    private boolean initiator;

    /**
     * Tests if this is the initiator side of the context.
     *
     * @return boolean indicating if this is initiator (true)
     *  or target (false)
     */
    public final boolean isInitiator() {
	return initiator;
    }

}

