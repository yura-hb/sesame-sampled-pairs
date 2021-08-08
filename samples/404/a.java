import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.jdt.internal.compiler.util.GenericXMLWriter;

class Main implements ProblemSeverities, SuffixConstants {
    class Logger {
	/**
		 * @param options the given compiler options
		 */
	public void logOptions(Map&lt;String, String&gt; options) {
	    if ((this.tagBits & Logger.XML) != 0) {
		printTag(Logger.OPTIONS, null, true, false);
		final Set&lt;Map.Entry&lt;String, String&gt;&gt; entriesSet = options.entrySet();
		Map.Entry&lt;String, String&gt;[] entries = entriesSet.toArray(new Map.Entry[entriesSet.size()]);
		Arrays.sort(entries, new Comparator&lt;Map.Entry&lt;String, String&gt;&gt;() {
		    @Override
		    public int compare(Map.Entry&lt;String, String&gt; o1, Map.Entry&lt;String, String&gt; o2) {
			Map.Entry&lt;String, String&gt; entry1 = o1;
			Map.Entry&lt;String, String&gt; entry2 = o2;
			return entry1.getKey().compareTo(entry2.getKey());
		    }
		});
		for (int i = 0, max = entries.length; i &lt; max; i++) {
		    Map.Entry&lt;String, String&gt; entry = entries[i];
		    String key = entry.getKey();
		    this.parameters.put(Logger.KEY, key);
		    this.parameters.put(Logger.VALUE, entry.getValue());
		    printTag(Logger.OPTION, this.parameters, true, true);
		}
		endTag(Logger.OPTIONS);
	    }
	}

	int tagBits;
	public static final int XML = 1;
	private static final String OPTIONS = "options";
	private HashMap&lt;String, Object&gt; parameters;
	private static final String KEY = "key";
	private static final String VALUE = "value";
	private static final String OPTION = "option";
	private PrintWriter log;

	private void printTag(String name, HashMap&lt;String, Object&gt; params, boolean insertNewLine, boolean closeTag) {
	    if (this.log != null) {
		((GenericXMLWriter) this.log).printTag(name, this.parameters, true, insertNewLine, closeTag);
	    }
	    this.parameters.clear();
	}

	private void endTag(String name) {
	    if (this.log != null) {
		((GenericXMLWriter) this.log).endTag(name, true, true);
	    }
	}

    }

}

