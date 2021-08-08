import java.util.ArrayList;
import java.util.Collections;

abstract class AbstractHeaderCheck extends AbstractFileSetCheck implements ExternalResourceHolder {
    /**
     * Return the header lines to check against.
     * @return the header lines to check against.
     */
    protected List&lt;String&gt; getHeaderLines() {
	final List&lt;String&gt; copy = new ArrayList&lt;&gt;(readerLines);
	return Collections.unmodifiableList(copy);
    }

    /** The lines of the header file. */
    private final List&lt;String&gt; readerLines = new ArrayList&lt;&gt;();

}

