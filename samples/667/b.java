class Assert {
    /**
     * Asserts that an object isn't null. If it is
     * an AssertionFailedError is thrown with the given message.
     */
    static public void assertNotNull(String message, Object object) {
	assertTrue(message, object != null);
    }

    /**
     * Asserts that a condition is true. If it isn't it throws
     * an AssertionFailedError with the given message.
     */
    static public void assertTrue(String message, boolean condition) {
	if (!condition)
	    fail(message);
    }

    /**
     * Fails a test with the given message.
     */
    static public void fail(String message) {
	throw new AssertionFailedError(message);
    }

}

