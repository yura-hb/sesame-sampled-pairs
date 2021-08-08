import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Stack;
import java.util.StringTokenizer;

class JemmyProperties {
    /**
     * Returns major version (like 1.0).
     *
     * @return a String representing the major version value.
     */
    public static String getMajorVersion() {
	return (extractValue(
		getProperties().getClass().getClassLoader().getResourceAsStream("org/netbeans/jemmy/version_info"),
		"Jemmy-MajorVersion"));
    }

    private static final Stack&lt;JemmyProperties&gt; propStack = new Stack&lt;&gt;();
    Hashtable&lt;String, Object&gt; properties;
    private static final int DEFAULT_DRAG_AND_DROP_STEP_LENGTH = 100;
    /**
     * Event shorcutting model mask. Should not be used together with robot
     * mask.
     *
     * @see #getCurrentDispatchingModel()
     * @see #setCurrentDispatchingModel(int)
     */
    public static final int SHORTCUT_MODEL_MASK = 4;
    /**
     * The event queue model mask.
     *
     * @see #getCurrentDispatchingModel()
     * @see #setCurrentDispatchingModel(int)
     */
    public static final int QUEUE_MODEL_MASK = 1;

    /**
     * Peeks upper JemmyProperties instance from stack.
     *
     * @return a JemmyProperties object representing the properties value.
     */
    public static JemmyProperties getProperties() {
	if (propStack.empty()) {
	    propStack.add(new JemmyProperties());
	}
	return propStack.peek();
    }

    private static String extractValue(InputStream stream, String varName) {
	try {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
	    StringTokenizer token;
	    String nextLine;
	    while ((nextLine = reader.readLine()) != null) {
		token = new StringTokenizer(nextLine, ":");
		String nextToken = token.nextToken();
		if (nextToken.trim().equals(varName)) {
		    return token.nextToken().trim();
		}
	    }
	    return "";
	} catch (IOException e) {
	    getCurrentOutput().printStackTrace(e);
	    return "";
	}
    }

    /**
     *
     */
    protected JemmyProperties() {
	super();
	properties = new Hashtable&lt;&gt;();
	setProperty("timeouts", new Timeouts());
	setProperty("output", new TestOut());
	setProperty("resources", new BundleManager());
	setProperty("binding.map", new DefaultCharBindingMap());
	setProperty("dispatching.model", getDefaultDispatchingModel());
	setProperty("drag_and_drop.step_length", DEFAULT_DRAG_AND_DROP_STEP_LENGTH);
    }

    /**
     * Just like getProperties().getOutput().
     *
     * @return a TestOut object representing the current output.
     * @see #setCurrentOutput
     */
    public static TestOut getCurrentOutput() {
	return getProperties().getOutput();
    }

    /**
     * Saves object as a static link to be used by other objects.
     *
     * @param name Property name. Should by unique.
     * @param newValue Property value.
     * @return Previous value of "name" property.
     * @see #setCurrentProperty(String, Object)
     * @see #getProperty(String)
     * @see #contains(String)
     */
    public Object setProperty(String name, Object newValue) {
	Object oldValue = null;
	if (contains(name)) {
	    oldValue = properties.get(name);
	    properties.remove(name);
	}
	properties.put(name, newValue);
	return oldValue;
    }

    /**
     * Returns default event dispatching model.
     *
     * @return QUEUE_MODEL_MASK
     * @see #setCurrentDispatchingModel(int)
     * @see #QUEUE_MODEL_MASK
     * @see #ROBOT_MODEL_MASK
     */
    public static int getDefaultDispatchingModel() {
	return SHORTCUT_MODEL_MASK | QUEUE_MODEL_MASK;
    }

    /**
     * Returns output.
     *
     * @return a TestOut object representing the output value
     * @see #setOutput
     */
    public TestOut getOutput() {
	return (TestOut) getProperty("output");
    }

    /**
     * Checks if "name" propery currently has a value.
     *
     * @param name Property name. Should by unique.
     * @return true if property was defined.
     * @see #setProperty(String, Object)
     * @see #getProperty(String)
     */
    public boolean contains(String name) {
	return properties.containsKey(name);
    }

    /**
     * Returns the property value.
     *
     * @param name Property name. Should by unique.
     * @return Property value stored by setProperty(String, Object) method.
     * @see #getCurrentProperty(String)
     * @see #setProperty(String, Object)
     * @see #contains(String)
     */
    public Object getProperty(String name) {
	if (contains(name)) {
	    return properties.get(name);
	} else {
	    return null;
	}
    }

}

