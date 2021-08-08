class JAXPTestUtilities {
    /**
     * Prints error message if an exception is thrown when clean up a file.
     * @param ex The exception is thrown in cleaning up a file.
     * @param name Cleaning up file name.
     */
    public static void failCleanup(IOException ex, String name) {
	fail(String.format(ERROR_MSG_CLEANUP, name), ex);
    }

    /**
     * Prefix for error message on clean up block.
     */
    public static final String ERROR_MSG_CLEANUP = "Clean up failed on %s";

}

