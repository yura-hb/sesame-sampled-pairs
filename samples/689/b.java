class FinalizerYes extends BaseClass {
    /**
     * Finalizer for the help class, that increments a counter
     * for each call to this method.
     */
    public final void finalize() {
	synchronized (this.getClass()) {
	    FinalizerYes.noOfFinalized++;
	}
    }

    private static int noOfFinalized = 0;

}

