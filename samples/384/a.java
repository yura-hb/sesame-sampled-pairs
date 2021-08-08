class ProcessorTestStatus {
    /** A processor can call this to indicate that it has run (with or without errors) */
    public static void setProcessorRan() {
	s_processorRan = true;
	if (NOT_RUN.equals(s_errorStatus))
	    s_errorStatus = NO_ERRORS;
    }

    /**
     * Was a processor run at all?
     */
    private static boolean s_processorRan = false;
    /**
     * Marker string to indicate processor never ran.
     */
    public static final String NOT_RUN = "NOT RUN";
    /** Error status. Will be == NO_ERRORS if no errors were encountered **/
    private static String s_errorStatus = NOT_RUN;
    /** 
     * Marker string to indicate that no errors were encountered.
     */
    public static final String NO_ERRORS = "NO ERRORS";

}

