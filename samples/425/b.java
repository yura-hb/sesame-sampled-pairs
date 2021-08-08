import java.util.HashSet;
import java.util.Set;

class HierarchyShape {
    /**
     * Get an iterator over all types.
     *
     * @return An iterator over all types.
     */
    public Collection&lt;Integer&gt; types() {
	final Set&lt;Integer&gt; combined = new HashSet(classes);
	combined.addAll(interfaces);
	return combined;
    }

    /**
     * The names of all the classes.
     */
    private final HashSet&lt;Integer&gt; classes;
    /**
     * The names of all the interfaces.
     */
    private final HashSet&lt;Integer&gt; interfaces;

}

