class StringUtils {
    /**
     * Get stack trace for a given thread.
     */
    public static String getStackTrace(Thread t) {
	final StackTraceElement[] stackTrace = t.getStackTrace();
	StringBuilder str = new StringBuilder();
	for (StackTraceElement e : stackTrace) {
	    str.append(e.toString() + "\n");
	}
	return str.toString();
    }

}

