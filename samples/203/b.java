class DocEnv {
    /**
     * Print a message.
     *
     * @param msg message to print.
     */
    public void printNotice(String msg) {
	if (silent || quiet)
	    return;
	messager.printNotice(msg);
    }

    /**
     * Set this to true if you would like to not emit any errors, warnings and
     * notices.
     */
    private boolean silent = false;
    /**
     * True if we do not want to print any notifications at all.
     */
    boolean quiet = false;
    private final Messager messager;

}

