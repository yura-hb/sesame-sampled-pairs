class KerberosKey implements SecretKey {
    /**
     * Returns the key version number.
     *
     * @return the key version number.
     * @throws IllegalStateException if the key is destroyed
     */
    public final int getVersionNumber() {
	if (destroyed) {
	    throw new IllegalStateException("This key is no longer valid");
	}
	return versionNum;
    }

    private transient boolean destroyed = false;
    /**
     * the version number of this secret key
     *
     * @serial
     */
    private final int versionNum;

}

