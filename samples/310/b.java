import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

class Timeouts {
    /**
     * Loads timeouts values from file.
     *
     * @param fileName File to load timeouts from.
     * @see org.netbeans.jemmy.Timeouts#load(InputStream)
     * @see org.netbeans.jemmy.Timeouts#load(String)
     * @exception IOException
     * @exception FileNotFoundException
     */
    public void load(String fileName) throws FileNotFoundException, IOException {
	try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
	    load(fileInputStream);
	}
    }

    private final Hashtable&lt;String, Long&gt; timeouts;
    private static final Timeouts defaults;
    private static double timeoutsScale = -1;

    /**
     * Loads timeouts values.
     *
     * @param stream Stream to load timeouts from.
     * @see org.netbeans.jemmy.Timeouts#load(String)
     * @see org.netbeans.jemmy.Timeouts#load()
     * @exception IOException
     */
    public void load(InputStream stream) throws IOException {
	Properties props = new Properties();
	props.load(stream);
	Enumeration&lt;?&gt; propNames = props.propertyNames();
	long propValue;
	String propName = null;
	while (propNames.hasMoreElements()) {
	    propName = (String) propNames.nextElement();
	    propValue = Long.parseLong(props.getProperty(propName));
	    setTimeout(propName, propValue);
	}
    }

    /**
     * Sets new timeout value.
     *
     * @param name Timeout name.
     * @param newValue Timeout value.
     * @return old timeout value
     * @see #getTimeout
     */
    public long setTimeout(String name, long newValue) {
	long oldValue = -1;
	if (contains(name)) {
	    oldValue = getTimeout(name);
	}
	timeouts.put(name, newValue);
	return oldValue;
    }

    /**
     * Checks if timeout has already been defined in this timeout instance.
     *
     * @param name Timeout name.
     * @return True if timeout has been defined, false otherwise.
     * @see #containsDefault(String)
     */
    public boolean contains(String name) {
	return timeouts.containsKey(name);
    }

    /**
     * Gets timeout value. It timeout was not defined in this instance, returns
     * default timeout value.
     *
     * @param name Timeout name.
     * @return Timeout value.
     * @see #getDefault(String)
     * @see #setTimeout
     */
    public long getTimeout(String name) {
	long timeout;
	if (contains(name) && timeouts.get(name) != null) {
	    timeout = timeouts.get(name);
	    timeout = (long) ((double) timeout * getTimeoutsScale());
	} else if (this != defaults) {
	    timeout = getDefault(name);
	} else {
	    timeout = -1;
	}
	return timeout;
    }

    /**
     * Get timeouts scale. Uses jemmy.timeouts.scale system property to get the
     * value.
     *
     * @return timeouts scale or 1 if the property is not set.
     */
    public static double getTimeoutsScale() {
	if (timeoutsScale == -1) {
	    String s = System.getProperty("jemmy.timeouts.scale", "1");
	    try {
		timeoutsScale = Double.parseDouble(s);
	    } catch (NumberFormatException e) {
		timeoutsScale = 1;
	    }
	}
	if (timeoutsScale &lt; 0) {
	    timeoutsScale = 1;
	}
	return timeoutsScale;
    }

    /**
     * Gets default timeout value.
     *
     * @param name Timeout name.
     * @return Timeout value or -1 if timeout is not defined.
     * @see #setDefault(String, long)
     * @see #initDefault(String, long)
     * @see #containsDefault(String)
     */
    public static long getDefault(String name) {
	return defaults.getTimeout(name);
    }

}

