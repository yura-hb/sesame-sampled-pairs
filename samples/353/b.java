import java.util.*;

class CheckResourceKeys {
    /**
     * Get the set of keys from the javadoc resource bundles.
     */
    Set&lt;String&gt; getResourceKeys() {
	Module jdk_javadoc = ModuleLayer.boot().findModule("jdk.javadoc").get();
	String[] names = { "com.sun.tools.javadoc.resources.javadoc", };
	Set&lt;String&gt; results = new TreeSet&lt;String&gt;();
	for (String name : names) {
	    ResourceBundle b = ResourceBundle.getBundle(name, jdk_javadoc);
	    results.addAll(b.keySet());
	}
	return results;
    }

}

