import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.util.*;

class Configuration implements Iterable&lt;Entry&lt;String, String&gt;&gt;, Writable, Serializable {
    /**
     * Clears all keys from the configuration.
     */
    public void clear() {
	getProps().clear();
	getOverlay().clear();
    }

    private Properties properties;
    /**
     * List of configuration resources.
     */
    private ArrayList&lt;Object&gt; resources = new ArrayList&lt;&gt;();
    private boolean quietmode = true;
    private Properties overlay;
    /**
     * Flag to indicate if the storage of resource which updates a key needs
     * to be stored for each key
     */
    private boolean storeResource;
    /**
     * Stores the mapping of key to the resource which modifies or loads
     * the key most recently
     */
    private HashMap&lt;String, String&gt; updatingResource;
    private boolean loadDefaults = true;
    /**
     * List of default Resources. Resources are loaded in the order of the list
     * entries
     */
    private static final CopyOnWriteArrayList&lt;String&gt; defaultResources = new CopyOnWriteArrayList&lt;&gt;();
    private static final Logger LOG = LoggerFactory.getLogger(Configuration.class);
    /**
     * List of configuration parameters marked &lt;b&gt;final&lt;/b&gt;.
     */
    private Set&lt;String&gt; finalParameters = new HashSet&lt;&gt;();
    private transient ClassLoader classLoader;

    private synchronized Properties getProps() {
	if (properties == null) {
	    properties = new Properties();
	    loadResources(properties, resources, quietmode);
	    if (overlay != null) {
		properties.putAll(overlay);
		if (storeResource) {
		    for (Map.Entry&lt;Object, Object&gt; item : overlay.entrySet()) {
			updatingResource.put((String) item.getKey(), "Unknown");
		    }
		}
	    }
	}
	return properties;
    }

    private synchronized Properties getOverlay() {
	if (overlay == null) {
	    overlay = new Properties();
	}
	return overlay;
    }

    private void loadResources(Properties properties, ArrayList resources, boolean quiet) {
	if (loadDefaults) {
	    // To avoid addResource causing a ConcurrentModificationException
	    ArrayList&lt;String&gt; toLoad;
	    synchronized (Configuration.class) {
		toLoad = new ArrayList&lt;&gt;(defaultResources);
	    }
	    for (String resource : toLoad) {
		loadResource(properties, resource, quiet);
	    }

	    //support the hadoop-site.xml as a deprecated case
	    if (getResource("hadoop-site.xml") != null) {
		loadResource(properties, "hadoop-site.xml", quiet);
	    }
	}

	for (Object resource : resources) {
	    loadResource(properties, resource, quiet);
	}
    }

    private void loadResource(Properties properties, Object name, boolean quiet) {
	try {
	    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	    //ignore all comments inside the xml file
	    docBuilderFactory.setIgnoringComments(true);

	    //allow includes in the xml file
	    docBuilderFactory.setNamespaceAware(true);
	    try {
		docBuilderFactory.setXIncludeAware(true);
	    } catch (UnsupportedOperationException e) {
		LOG.error("Failed to set setXIncludeAware(true) for parser " + docBuilderFactory + ":" + e, e);
	    }
	    DocumentBuilder builder = docBuilderFactory.newDocumentBuilder();
	    Document doc = null;
	    Element root = null;

	    if (name instanceof URL) { // an URL resource
		URL url = (URL) name;
		if (url != null) {
		    if (!quiet) {
			LOG.info("parsing " + url);
		    }
		    doc = builder.parse(url.toString());
		}
	    } else if (name instanceof String) { // a CLASSPATH resource
		URL url = getResource((String) name);
		if (url != null) {
		    if (!quiet) {
			LOG.info("parsing " + url);
		    }
		    doc = builder.parse(url.toString());
		}
	    } else if (name instanceof InputStream) {
		try {
		    doc = builder.parse((InputStream) name);
		} finally {
		    ((InputStream) name).close();
		}
	    } else if (name instanceof Element) {
		root = (Element) name;
	    }

	    if (doc == null && root == null) {
		if (quiet)
		    return;
		throw new RuntimeException(name + " not found");
	    }

	    if (root == null) {
		root = doc.getDocumentElement();
	    }
	    if (!"configuration".equals(root.getTagName()))
		LOG.error("bad conf file: top-level element not &lt;configuration&gt;");
	    NodeList props = root.getChildNodes();
	    for (int i = 0; i &lt; props.getLength(); i++) {
		Node propNode = props.item(i);
		if (!(propNode instanceof Element))
		    continue;
		Element prop = (Element) propNode;
		if ("configuration".equals(prop.getTagName())) {
		    loadResource(properties, prop, quiet);
		    continue;
		}
		if (!"property".equals(prop.getTagName()))
		    LOG.warn("bad conf file: element not &lt;property&gt;");
		NodeList fields = prop.getChildNodes();
		String attr = null;
		String value = null;
		boolean finalParameter = false;
		for (int j = 0; j &lt; fields.getLength(); j++) {
		    Node fieldNode = fields.item(j);
		    if (!(fieldNode instanceof Element))
			continue;
		    Element field = (Element) fieldNode;
		    if ("name".equals(field.getTagName()) && field.hasChildNodes())
			attr = ((Text) field.getFirstChild()).getData().trim();
		    if ("value".equals(field.getTagName()) && field.hasChildNodes())
			value = ((Text) field.getFirstChild()).getData();
		    if ("final".equals(field.getTagName()) && field.hasChildNodes())
			finalParameter = "true".equals(((Text) field.getFirstChild()).getData());
		}

		// Ignore this parameter if it has already been marked as 'final'
		if (attr != null && value != null) {
		    if (!finalParameters.contains(attr)) {
			properties.setProperty(attr, value);
			if (storeResource) {
			    updatingResource.put(attr, name.toString());
			}
			if (finalParameter)
			    finalParameters.add(attr);
		    } else {
			LOG.warn(name + ":a attempt to override final parameter: " + attr + ";  Ignoring.");
		    }
		}
	    }

	} catch (IOException | ParserConfigurationException | SAXException | DOMException e) {
	    LOG.error("error parsing conf file: " + e);
	    throw new RuntimeException(e);
	}
    }

    /**
     * Get the {@link URL} for the named resource.
     *
     * @param name resource name.
     * @return the url for the named resource.
     */
    public URL getResource(String name) {
	return classLoader.getResource(name);
    }

}

