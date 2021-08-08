import java.util.*;
import javax.lang.model.element.ModuleElement;
import jdk.javadoc.internal.doclets.toolkit.BaseConfiguration;

class IndexBuilder {
    /**
     * Add all the modules to index map.
     */
    protected void addModulesToIndexMap() {
	for (ModuleElement mdle : configuration.modules) {
	    String mdleName = mdle.getQualifiedName().toString();
	    char ch = (mdleName.length() == 0) ? '*' : Character.toUpperCase(mdleName.charAt(0));
	    Character unicode = ch;
	    SortedSet&lt;Element&gt; list = indexmap.computeIfAbsent(unicode, c -&gt; new TreeSet&lt;&gt;(comparator));
	    list.add(mdle);
	}
    }

    private final BaseConfiguration configuration;
    /**
     * Mapping of each Unicode Character with the member list containing
     * members with names starting with it.
     */
    private final Map&lt;Character, SortedSet&lt;Element&gt;&gt; indexmap;
    private final Comparator&lt;Element&gt; comparator;

}

