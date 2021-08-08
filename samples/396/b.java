class LocalProcess extends FinalizableObject {
    /** Check whether the process has been terminated. */
    public boolean terminated() {
	try {
	    int value = process.exitValue();
	    return true;
	} catch (IllegalThreadStateException e) {
	    return false;
	}
    }

    private Process process;

}

