import java.util.ArrayList;
import java.util.Collections;

class TestLogHandler extends Handler {
    /** Returns a snapshot of the logged records. */
    /*
     * TODO(cpovirk): consider higher-level APIs here (say, assertNoRecordsLogged(),
     * getOnlyRecordLogged(), getAndClearLogRecords()...)
     *
     * TODO(cpovirk): consider renaming this method to reflect that it takes a snapshot (and/or return
     * an ImmutableList)
     */
    public synchronized List&lt;LogRecord&gt; getStoredLogRecords() {
	List&lt;LogRecord&gt; result = new ArrayList&lt;&gt;(list);
	return Collections.unmodifiableList(result);
    }

    /** We will keep a private list of all logged records */
    private final List&lt;LogRecord&gt; list = new ArrayList&lt;&gt;();

}

