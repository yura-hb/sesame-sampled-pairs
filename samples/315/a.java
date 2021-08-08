import java.util.Vector;
import java.util.Enumeration;

class TestResult {
    /**
     * Informs the result that a test was completed.
     */
    public void endTest(Test test) {
	for (Enumeration e = cloneListeners().elements(); e.hasMoreElements();) {
	    ((TestListener) e.nextElement()).endTest(test);
	}
    }

    protected Vector fListeners;

    /**
     * Returns a copy of the listeners.
     */
    private synchronized Vector cloneListeners() {
	return (Vector) fListeners.clone();
    }

}

