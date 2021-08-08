import java.util.Vector;
import java.io.Writer;

class PerformanceLogger {
    /**
     * Outputs all data to parameter-specified Writer object
     */
    public static void outputLog(Writer writer) {
	if (loggingEnabled()) {
	    try {
		synchronized (times) {
		    for (int i = 0; i &lt; times.size(); ++i) {
			TimeData td = times.get(i);
			if (td != null) {
			    writer.write(i + " " + td.getMessage() + ": " + (td.getTime() - baseTime) + "\n");

			}
		    }
		}
		writer.flush();
	    } catch (Exception e) {
		System.out.println(e + ": Writing performance log to " + writer);
	    }
	}
    }

    private static Vector&lt;TimeData&gt; times;
    private static long baseTime;
    private static boolean perfLoggingOn = false;

    /**
     * Returns status of whether logging is enabled or not.  This is
     * provided as a convenience method so that users do not have to
     * perform the same GetPropertyAction check as above to determine whether
     * to enable performance logging.
     */
    public static boolean loggingEnabled() {
	return perfLoggingOn;
    }

    class TimeData {
	private static Vector&lt;TimeData&gt; times;
	private static long baseTime;
	private static boolean perfLoggingOn = false;

	String getMessage() {
	    return message;
	}

	long getTime() {
	    return time;
	}

    }

}

