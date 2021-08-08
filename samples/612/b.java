import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.Debug;
import jdk.nashorn.internal.runtime.ScriptObject;
import jdk.nashorn.internal.runtime.logging.DebugLogger;

class MethodHandleFactory {
    /**
     * Tracer that is applied before a function is called, printing the arguments
     *
     * @param tag  tag to start the debug printout string
     * @param paramStart param index to start outputting from
     * @param args arguments to the function
     */
    static void traceArgs(final DebugLogger logger, final String tag, final int paramStart, final Object... args) {
	final StringBuilder sb = new StringBuilder();

	sb.append(tag);

	for (int i = paramStart; i &lt; args.length; i++) {
	    if (i == paramStart) {
		sb.append(" =&gt; args: ");
	    }

	    sb.append('\'').append(stripName(argString(args[i]))).append('\'').append(' ').append('[').append("type=")
		    .append(args[i] == null ? "null" : stripName(args[i].getClass())).append(']');

	    if (i + 1 &lt; args.length) {
		sb.append(", ");
	    }
	}

	if (logger == null) {
	    err(sb.toString());
	} else {
	    logger.log(TRACE_LEVEL, sb);
	}
	stacktrace(logger);
    }

    private static final Level TRACE_LEVEL = Level.INFO;
    private static final boolean PRINT_STACKTRACE = Options
	    .getBooleanProperty("nashorn.methodhandles.debug.stacktrace");

    private static String argString(final Object arg) {
	if (arg == null) {
	    return "null";
	}

	if (arg.getClass().isArray()) {
	    final List&lt;Object&gt; list = new ArrayList&lt;&gt;();
	    for (final Object elem : (Object[]) arg) {
		list.add('\'' + argString(elem) + '\'');
	    }

	    return list.toString();
	}

	if (arg instanceof ScriptObject) {
	    return arg.toString() + " (map=" + Debug.id(((ScriptObject) arg).getMap()) + ')';
	}

	return arg.toString();
    }

    /**
     * Helper function that takes a class or an object with a toString override
     * and shortens it to notation after last dot. This is used to facilitiate
     * pretty printouts in various debug loggers - internal only
     *
     * @param obj class or object
     *
     * @return pretty version of object as string
     */
    public static String stripName(final Object obj) {
	if (obj == null) {
	    return "null";
	}

	if (obj instanceof Class) {
	    return ((Class&lt;?&gt;) obj).getSimpleName();
	}
	return obj.toString();
    }

    private static void err(final String str) {
	Context.getContext().getErr().println(str);
    }

    private static void stacktrace(final DebugLogger logger) {
	if (!PRINT_STACKTRACE) {
	    return;
	}
	final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	final PrintStream ps = new PrintStream(baos);
	new Throwable().printStackTrace(ps);
	final String st = baos.toString();
	if (logger == null) {
	    err(st);
	} else {
	    logger.log(TRACE_LEVEL, st);
	}
    }

}

