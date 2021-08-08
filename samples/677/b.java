import java.io.PrintStream;
import java.util.Vector;

class Log extends FinalizableObject {
    /**
     * Enable or disable verbose mode for printing messages.
     */
    public void enableVerbose(boolean enable) {
	if (!verbose) {
	    flushLogBuffer();
	}
	verbose = enable;
    }

    /**
     * Is log-mode verbose?
     * Default value is &lt;code&gt;false&lt;/code&gt;.
     */
    private boolean verbose = false;
    /**
     * This &lt;code&gt;logBuffer&lt;/code&gt; will keep all messages printed via
     * &lt;code&gt;display()&lt;/code&gt; method in non-verbose mode until
     * swithching verbose mode on or invoking &lt;code&gt;complain()&lt;/code&gt;.
     * Ensure that buffer has enough room for messages to keep,
     * to minimize probability or OutOfMemory error while keeping
     * an error message in stress tests.
     */
    private Vector&lt;String&gt; logBuffer = new Vector&lt;String&gt;(1000);
    /**
     * Report step-by-step activity to this stream.
     *
     * @deprecated  Tests should not use this field directly.
     */
    protected PrintStream out = null;
    /**
     * Did I already warned if output stream is not assigned?
     */
    private boolean noOutWarned = false;

    /**
     * Print all messages from log buffer which were hidden because
     * of non-verbose mode,
     */
    private synchronized void flushLogBuffer() {
	if (!logBuffer.isEmpty()) {
	    PrintStream stream = findOutStream();
	    for (int i = 0; i &lt; logBuffer.size(); i++) {
		stream.println(logBuffer.elementAt(i));
	    }
	    stream.flush();
	}
    }

    /**
     * Return &lt;code&gt;out&lt;/code&gt; stream if defined or &lt;code&gt;Sytem.err&lt;code&gt; otherwise;
     * print a warning message when &lt;code&gt;System.err&lt;/code&gt; is used first time.
     */
    private synchronized PrintStream findOutStream() {
	PrintStream stream = out;
	if (stream == null) {
	    stream = System.err;
	    if (!noOutWarned) {
		noOutWarned = true;
		stream.println("#&gt;  ");
		stream.println("#&gt;  WARNING: switching log stream to stderr,");
		stream.println("#&gt;      because no output stream is assigned");
		stream.println("#&gt;  ");
	    }
	    ;
	}
	;
	stream.flush();
	return stream;
    }

}

