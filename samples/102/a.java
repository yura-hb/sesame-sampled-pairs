abstract class BaseProcessor extends AbstractProcessor {
    /**
     * Report an error to the test case code
     * @param value
     */
    public void reportError(String value) {
	// Debugging - don't report error
	// value = "succeeded";
	System.setProperty(this.getClass().getName(), value);
    }

}

