class GHASH {
    /**
     * Restores this object using the saved snapshot.
     */
    void restore() {
	state[0] = stateSave0;
	state[1] = stateSave1;
    }

    private final long[] state;
    private long stateSave0, stateSave1;
    private long stateSave0, stateSave1;

}

