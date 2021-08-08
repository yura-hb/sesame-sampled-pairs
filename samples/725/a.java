import java.util.Vector;

class TestResult {
    /**
     * Returns whether the entire test was successful or not.
     */
    public synchronized boolean wasSuccessful() {
	return failureCount() == 0 && errorCount() == 0;
    }

    protected Vector fFailures;
    protected Vector fErrors;

    /**
     * Gets the number of detected failures.
     */
    public synchronized int failureCount() {
	return fFailures.size();
    }

    /**
     * Gets the number of detected errors.
     */
    public synchronized int errorCount() {
	return fErrors.size();
    }

}

