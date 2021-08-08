import java.util.Collections;
import java.util.LinkedList;

class MapModuleManager {
    /** @return an unmodifiable set of all display names of current opened maps. */
    public List&lt;String&gt; getMapKeys() {
	LinkedList&lt;String&gt; returnValue = new LinkedList&lt;&gt;();
	for (MapModule module : mapModuleVector) {
	    returnValue.add(module.getDisplayName());
	}
	return Collections.unmodifiableList(returnValue);
    }

    /**
     * A vector of MapModule instances. They are ordered according to their
     * screen order.
     */
    private Vector&lt;MapModule&gt; mapModuleVector = new Vector&lt;&gt;();

}

