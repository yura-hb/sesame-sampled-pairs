import java.io.File;
import java.nio.file.Files;
import com.puppycrawl.tools.checkstyle.DefaultLogger;
import com.puppycrawl.tools.checkstyle.XMLLogger;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;

class CheckstyleAntTask extends Task {
    class Formatter {
	/**
	 * Creates a listener for the formatter.
	 * @param task the task running
	 * @return a listener
	 * @throws IOException if an error occurs
	 */
	public AuditListener createListener(Task task) throws IOException {
	    final AuditListener listener;
	    if (type != null && E_XML.equals(type.getValue())) {
		listener = createXmlLogger(task);
	    } else {
		listener = createDefaultLogger(task);
	    }
	    return listener;
	}

	/** The formatter type. */
	private FormatterType type;
	/** The file to output to. */
	private File toFile;
	/** Whether or not the write to the named file. */
	private boolean useFile = true;

	/**
	 * Creates XML logger.
	 * @param task the task to possibly log to
	 * @return an XMLLogger instance
	 * @throws IOException if an error occurs
	 */
	private AuditListener createXmlLogger(Task task) throws IOException {
	    final AuditListener xmlLogger;
	    if (toFile == null || !useFile) {
		xmlLogger = new XMLLogger(new LogOutputStream(task, Project.MSG_INFO),
			AutomaticBean.OutputStreamOptions.CLOSE);
	    } else {
		xmlLogger = new XMLLogger(Files.newOutputStream(toFile.toPath()),
			AutomaticBean.OutputStreamOptions.CLOSE);
	    }
	    return xmlLogger;
	}

	/**
	 * Creates default logger.
	 * @param task the task to possibly log to
	 * @return a DefaultLogger instance
	 * @throws IOException if an error occurs
	 */
	private AuditListener createDefaultLogger(Task task) throws IOException {
	    final AuditListener defaultLogger;
	    if (toFile == null || !useFile) {
		defaultLogger = new DefaultLogger(new LogOutputStream(task, Project.MSG_DEBUG),
			AutomaticBean.OutputStreamOptions.CLOSE, new LogOutputStream(task, Project.MSG_ERR),
			AutomaticBean.OutputStreamOptions.CLOSE);
	    } else {
		final OutputStream infoStream = Files.newOutputStream(toFile.toPath());
		defaultLogger = new DefaultLogger(infoStream, AutomaticBean.OutputStreamOptions.CLOSE, infoStream,
			AutomaticBean.OutputStreamOptions.NONE);
	    }
	    return defaultLogger;
	}

    }

    /** Poor man's enum for an xml formatter. */
    private static final String E_XML = "xml";

}

