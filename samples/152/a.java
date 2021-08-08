import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class Configuration implements Iterable&lt;Entry&lt;String, String&gt;&gt;, Writable, Serializable {
    /**
     * Load a class by name.
     *
     * @param name the class name.
     * @return the class object.
     * @throws ClassNotFoundException if the class is not found.
     */
    public Class&lt;?&gt; getClassByName(String name) throws ClassNotFoundException {
	Map&lt;String, Class&lt;?&gt;&gt; map = CACHE_CLASSES.get(classLoader);
	if (map == null) {
	    Map&lt;String, Class&lt;?&gt;&gt; newMap = new ConcurrentHashMap&lt;&gt;();
	    map = CACHE_CLASSES.putIfAbsent(classLoader, newMap);
	    if (map == null) {
		map = newMap;
	    }
	}

	Class clazz = map.get(name);
	if (clazz == null) {
	    clazz = Class.forName(name, true, classLoader);
	    if (clazz != null) {
		map.put(name, clazz);
	    }
	}

	return clazz;
    }

    private static final ConcurrentMap&lt;ClassLoader, Map&lt;String, Class&lt;?&gt;&gt;&gt; CACHE_CLASSES = new ConcurrentHashMap&lt;&gt;();
    private transient ClassLoader classLoader;

}

