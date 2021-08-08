import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

class XMLFormatter extends Formatter {
    /**
     * Format the given message to XML.
     * &lt;p&gt;
     * This method can be overridden in a subclass.
     * It is recommended to use the {@link Formatter#formatMessage}
     * convenience method to localize and format the message field.
     *
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    @Override
    public String format(LogRecord record) {
	StringBuilder sb = new StringBuilder(500);
	sb.append("&lt;record&gt;\n");

	final Instant instant = record.getInstant();

	sb.append("  &lt;date&gt;");
	if (useInstant) {
	    // If useInstant is true - we will print the instant in the
	    // date field, using the ISO_INSTANT formatter.
	    DateTimeFormatter.ISO_INSTANT.formatTo(instant, sb);
	} else {
	    // If useInstant is false - we will keep the 'old' formating
	    appendISO8601(sb, instant.toEpochMilli());
	}
	sb.append("&lt;/date&gt;\n");

	sb.append("  &lt;millis&gt;");
	sb.append(instant.toEpochMilli());
	sb.append("&lt;/millis&gt;\n");

	final int nanoAdjustment = instant.getNano() % 1000_000;
	if (useInstant && nanoAdjustment != 0) {
	    sb.append("  &lt;nanos&gt;");
	    sb.append(nanoAdjustment);
	    sb.append("&lt;/nanos&gt;\n");
	}

	sb.append("  &lt;sequence&gt;");
	sb.append(record.getSequenceNumber());
	sb.append("&lt;/sequence&gt;\n");

	String name = record.getLoggerName();
	if (name != null) {
	    sb.append("  &lt;logger&gt;");
	    escape(sb, name);
	    sb.append("&lt;/logger&gt;\n");
	}

	sb.append("  &lt;level&gt;");
	escape(sb, record.getLevel().toString());
	sb.append("&lt;/level&gt;\n");

	if (record.getSourceClassName() != null) {
	    sb.append("  &lt;class&gt;");
	    escape(sb, record.getSourceClassName());
	    sb.append("&lt;/class&gt;\n");
	}

	if (record.getSourceMethodName() != null) {
	    sb.append("  &lt;method&gt;");
	    escape(sb, record.getSourceMethodName());
	    sb.append("&lt;/method&gt;\n");
	}

	sb.append("  &lt;thread&gt;");
	sb.append(record.getThreadID());
	sb.append("&lt;/thread&gt;\n");

	if (record.getMessage() != null) {
	    // Format the message string and its accompanying parameters.
	    String message = formatMessage(record);
	    sb.append("  &lt;message&gt;");
	    escape(sb, message);
	    sb.append("&lt;/message&gt;");
	    sb.append("\n");
	}

	// If the message is being localized, output the key, resource
	// bundle name, and params.
	ResourceBundle bundle = record.getResourceBundle();
	try {
	    if (bundle != null && bundle.getString(record.getMessage()) != null) {
		sb.append("  &lt;key&gt;");
		escape(sb, record.getMessage());
		sb.append("&lt;/key&gt;\n");
		sb.append("  &lt;catalog&gt;");
		escape(sb, record.getResourceBundleName());
		sb.append("&lt;/catalog&gt;\n");
	    }
	} catch (Exception ex) {
	    // The message is not in the catalog.  Drop through.
	}

	Object parameters[] = record.getParameters();
	//  Check to see if the parameter was not a messagetext format
	//  or was not null or empty
	if (parameters != null && parameters.length != 0 && record.getMessage().indexOf('{') == -1) {
	    for (Object parameter : parameters) {
		sb.append("  &lt;param&gt;");
		try {
		    escape(sb, parameter.toString());
		} catch (Exception ex) {
		    sb.append("???");
		}
		sb.append("&lt;/param&gt;\n");
	    }
	}

	if (record.getThrown() != null) {
	    // Report on the state of the throwable.
	    Throwable th = record.getThrown();
	    sb.append("  &lt;exception&gt;\n");
	    sb.append("    &lt;message&gt;");
	    escape(sb, th.toString());
	    sb.append("&lt;/message&gt;\n");
	    StackTraceElement trace[] = th.getStackTrace();
	    for (StackTraceElement frame : trace) {
		sb.append("    &lt;frame&gt;\n");
		sb.append("      &lt;class&gt;");
		escape(sb, frame.getClassName());
		sb.append("&lt;/class&gt;\n");
		sb.append("      &lt;method&gt;");
		escape(sb, frame.getMethodName());
		sb.append("&lt;/method&gt;\n");
		// Check for a line number.
		if (frame.getLineNumber() &gt;= 0) {
		    sb.append("      &lt;line&gt;");
		    sb.append(frame.getLineNumber());
		    sb.append("&lt;/line&gt;\n");
		}
		sb.append("    &lt;/frame&gt;\n");
	    }
	    sb.append("  &lt;/exception&gt;\n");
	}

	sb.append("&lt;/record&gt;\n");
	return sb.toString();
    }

    private final boolean useInstant;

    private void appendISO8601(StringBuilder sb, long millis) {
	GregorianCalendar cal = new GregorianCalendar();
	cal.setTimeInMillis(millis);
	sb.append(cal.get(Calendar.YEAR));
	sb.append('-');
	a2(sb, cal.get(Calendar.MONTH) + 1);
	sb.append('-');
	a2(sb, cal.get(Calendar.DAY_OF_MONTH));
	sb.append('T');
	a2(sb, cal.get(Calendar.HOUR_OF_DAY));
	sb.append(':');
	a2(sb, cal.get(Calendar.MINUTE));
	sb.append(':');
	a2(sb, cal.get(Calendar.SECOND));
    }

    private void escape(StringBuilder sb, String text) {
	if (text == null) {
	    text = "&lt;null&gt;";
	}
	for (int i = 0; i &lt; text.length(); i++) {
	    char ch = text.charAt(i);
	    if (ch == '&lt;') {
		sb.append("&lt;");
	    } else if (ch == '&gt;') {
		sb.append("&gt;");
	    } else if (ch == '&') {
		sb.append("&amp;");
	    } else {
		sb.append(ch);
	    }
	}
    }

    private void a2(StringBuilder sb, int x) {
	if (x &lt; 10) {
	    sb.append('0');
	}
	sb.append(x);
    }

}

