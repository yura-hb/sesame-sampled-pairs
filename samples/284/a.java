class Assert {
    /**
     * Asserts that two Strings are equal. 
     */
    static public void assertEquals(String message, String expected, String actual) {
	if (expected == null && actual == null)
	    return;
	if (expected != null && expected.equals(actual))
	    return;
	throw new ComparisonFailure(message, expected, actual);
    }

}

