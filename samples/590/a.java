import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import org.eclipse.jdt.internal.compiler.util.GenericXMLWriter;
import org.eclipse.jdt.internal.compiler.util.Util;

class Main implements ProblemSeverities, SuffixConstants {
    class Logger {
	/**
		 * @param e the given exception to log
		 */
	public void logException(Exception e) {
	    StringWriter writer = new StringWriter();
	    PrintWriter printWriter = new PrintWriter(writer);
	    e.printStackTrace(printWriter);
	    printWriter.flush();
	    printWriter.close();
	    final String stackTrace = writer.toString();
	    if ((this.tagBits & Logger.XML) != 0) {
		LineNumberReader reader = new LineNumberReader(new StringReader(stackTrace));
		String line;
		int i = 0;
		StringBuffer buffer = new StringBuffer();
		String message = e.getMessage();
		if (message != null) {
		    buffer.append(message).append(Util.LINE_SEPARATOR);
		}
		try {
		    while ((line = reader.readLine()) != null && i &lt; 4) {
			buffer.append(line).append(Util.LINE_SEPARATOR);
			i++;
		    }
		    reader.close();
		} catch (IOException e1) {
		    // ignore
		}
		message = buffer.toString();
		this.parameters.put(Logger.MESSAGE, message);
		this.parameters.put(Logger.CLASS, e.getClass());
		printTag(Logger.EXCEPTION, this.parameters, true, true);
	    }
	    String message = e.getMessage();
	    if (message == null) {
		this.printlnErr(stackTrace);
	    } else {
		this.printlnErr(message);
	    }
	}

	int tagBits;
	public static final int XML = 1;
	private HashMap&lt;String, Object&gt; parameters;
	private static final String MESSAGE = "message";
	private static final String CLASS = "class";
	private static final String EXCEPTION = "exception";
	private PrintWriter log;
	private PrintWriter err;

	private void printTag(String name, HashMap&lt;String, Object&gt; params, boolean insertNewLine, boolean closeTag) {
	    if (this.log != null) {
		((GenericXMLWriter) this.log).printTag(name, this.parameters, true, insertNewLine, closeTag);
	    }
	    this.parameters.clear();
	}

	private void printlnErr(String s) {
	    this.err.println(s);
	    if ((this.tagBits & Logger.XML) == 0 && this.log != null) {
		this.log.println(s);
	    }
	}

    }

}

