class KerberosCredMessage implements Destroyable {
    /**
     * Returns the sender of this message.
     *
     * @return the sender
     * @throws IllegalStateException if the object is destroyed
     */
    public KerberosPrincipal getSender() {
	if (destroyed) {
	    throw new IllegalStateException("This object is no longer valid");
	}
	return sender;
    }

    private boolean destroyed = false;
    final private KerberosPrincipal sender;

}

