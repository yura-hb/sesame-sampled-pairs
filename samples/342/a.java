abstract class BaseTestRunner implements TestListener {
    /**
     * Truncates a String to the maximum length.
     */
    public static String truncate(String s) {
	if (fgMaxMessageLength != -1 && s.length() &gt; fgMaxMessageLength)
	    s = s.substring(0, fgMaxMessageLength) + "...";
	return s;
    }

    static int fgMaxMessageLength = 500;

}

