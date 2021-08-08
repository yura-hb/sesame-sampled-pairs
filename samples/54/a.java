import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

class StdFormatter extends SimpleFormatter {
    /**
     * Format the given LogRecord.
     * 
     * @param record
     *            the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format(LogRecord record) {
	if (!STDERR.getName().equals(record.getLoggerName()) && !STDOUT.getName().equals(record.getLoggerName())) {
	    return super.format(record);
	}
	StringBuffer sb = new StringBuffer();
	sb.append(lineSeparator);
	String message = formatMessage(record);
	sb.append(record.getLevel().getLocalizedName());
	sb.append(": ");
	sb.append(message);
	return sb.toString();
    }

    /**
     * Level for STDERR activity
     */
    final static Level STDERR = new StdOutErrLevel("STDERR", Level.SEVERE.intValue() + 53);
    /**
     * Level for STDOUT activity.
     */
    final static Level STDOUT = new StdOutErrLevel("STDOUT", Level.WARNING.intValue() + 53);
    private String lineSeparator = System.getProperty("line.separator");

}

