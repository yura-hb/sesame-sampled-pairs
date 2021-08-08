class Expirable&lt;V&gt; {
    /** Returns if the value will never expire. */
    public boolean isEternal() {
	return (expireTimeMS == Long.MAX_VALUE);
    }

    private volatile long expireTimeMS;

}

