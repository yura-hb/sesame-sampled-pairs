import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.LocalizedMessage;

abstract class AbstractTypeAwareCheck extends AbstractCheck {
    /**
     * Common implementation for logLoadError() method.
     * @param lineNo line number of the problem.
     * @param columnNo column number of the problem.
     * @param msgKey message key to use.
     * @param values values to fill the message out.
     */
    protected final void logLoadErrorImpl(int lineNo, int columnNo, String msgKey, Object... values) {
	if (!logLoadErrors) {
	    final LocalizedMessage msg = new LocalizedMessage(lineNo, columnNo, getMessageBundle(), msgKey, values,
		    getSeverityLevel(), getId(), getClass(), null);
	    throw new IllegalStateException(msg.getMessage());
	}

	if (!suppressLoadErrors) {
	    log(lineNo, columnNo, msgKey, values);
	}
    }

    /**
     * Whether to log class loading errors to the checkstyle report
     * instead of throwing a RTE.
     *
     * &lt;p&gt;Logging errors will avoid stopping checkstyle completely
     * because of a typo in javadoc. However, with modern IDEs that
     * support automated refactoring and generate javadoc this will
     * occur rarely, so by default we assume a configuration problem
     * in the checkstyle classpath and throw an exception.
     *
     * &lt;p&gt;This configuration option was triggered by bug 1422462.
     */
    private boolean logLoadErrors = true;
    /**
     * Whether to show class loading errors in the checkstyle report.
     * Request ID 1491630
     */
    private boolean suppressLoadErrors;

}

