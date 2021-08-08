import java.util.Collections;
import java.util.HashMap;

class MapModuleManager {
    /**
     * @return a map of String to MapModule elements.
     * @deprecated use getMapModuleVector instead (and get the displayname as
     *             MapModule.getDisplayName().
     */
    public Map&lt;String, MapModule&gt; getMapModules() {
	HashMap&lt;String, MapModule&gt; returnValue = new HashMap&lt;&gt;();
	for (MapModule module : mapModuleVector) {
	    returnValue.put(module.getDisplayName(), module);
	}
	return Collections.unmodifiableMap(returnValue);
    }

    /**
     * A vector of MapModule instances. They are ordered according to their
     * screen order.
     */
    private Vector&lt;MapModule&gt; mapModuleVector = new Vector&lt;&gt;();

}

