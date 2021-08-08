import java.lang.reflect.*;

abstract class SwingTestHelper {
    /**
     * Invoke if the test should be considered to have failed.  This will
     * stop test execution.
     */
    public void fail(Throwable error) {
	synchronized (this) {
	    if (this.error == null) {
		if (error instanceof InvocationTargetException) {
		    this.error = ((InvocationTargetException) error).getCause();
		} else {
		    this.error = error;
		}
		this.done = true;
		notifyAll();
	    }
	}
    }

    private Throwable error;
    private boolean done;

}

