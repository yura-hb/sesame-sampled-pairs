import java.io.BufferedInputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.xml.sax.InputSource;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

class PackageNamesLoader extends XmlLoader {
    /**
     * Returns the set of package names, compiled from all
     * checkstyle_packages.xml files found on the given class loaders
     * classpath.
     * @param classLoader the class loader for loading the
     *          checkstyle_packages.xml files.
     * @return the set of package names.
     * @throws CheckstyleException if an error occurs.
     */
    public static Set&lt;String&gt; getPackageNames(ClassLoader classLoader) throws CheckstyleException {
	final Set&lt;String&gt; result;
	try {
	    //create the loader outside the loop to prevent PackageObjectFactory
	    //being created anew for each file
	    final PackageNamesLoader namesLoader = new PackageNamesLoader();

	    final Enumeration&lt;URL&gt; packageFiles = classLoader.getResources(CHECKSTYLE_PACKAGES);

	    while (packageFiles.hasMoreElements()) {
		processFile(packageFiles.nextElement(), namesLoader);
	    }

	    result = namesLoader.packageNames;
	} catch (IOException ex) {
	    throw new CheckstyleException("unable to get package file resources", ex);
	} catch (ParserConfigurationException | SAXException ex) {
	    throw new CheckstyleException("unable to open one of package files", ex);
	}

	return result;
    }

    /** Name of default checkstyle package names resource file.
     * The file must be in the classpath.
     */
    private static final String CHECKSTYLE_PACKAGES = "checkstyle_packages.xml";
    /** The fully qualified package names. */
    private final Set&lt;String&gt; packageNames = new LinkedHashSet&lt;&gt;();
    /** The public ID for the configuration dtd. */
    private static final String DTD_PUBLIC_ID = "-//Puppy Crawl//DTD Package Names 1.0//EN";
    /** The resource for the configuration dtd. */
    private static final String DTD_RESOURCE_NAME = "com/puppycrawl/tools/checkstyle/packages_1_0.dtd";
    /** The new public ID for the configuration dtd. */
    private static final String DTD_PUBLIC_CS_ID = "-//Checkstyle//DTD Package Names Configuration 1.0//EN";

    /**
     * Creates a new {@code PackageNamesLoader} instance.
     * @throws ParserConfigurationException if an error occurs
     * @throws SAXException if an error occurs
     */
    private PackageNamesLoader() throws ParserConfigurationException, SAXException {
	super(createIdToResourceNameMap());
    }

    /**
     * Reads the file provided and parses it with package names loader.
     * @param packageFile file from package
     * @param namesLoader package names loader
     * @throws SAXException if an error while parsing occurs
     * @throws CheckstyleException if unable to open file
     */
    private static void processFile(URL packageFile, PackageNamesLoader namesLoader)
	    throws SAXException, CheckstyleException {
	try (InputStream stream = new BufferedInputStream(packageFile.openStream())) {
	    final InputSource source = new InputSource(stream);
	    namesLoader.parseInputSource(source);
	} catch (IOException ex) {
	    throw new CheckstyleException("unable to open " + packageFile, ex);
	}
    }

    /**
     * Creates mapping between local resources and dtd ids.
     * @return map between local resources and dtd ids.
     */
    private static Map&lt;String, String&gt; createIdToResourceNameMap() {
	final Map&lt;String, String&gt; map = new HashMap&lt;&gt;();
	map.put(DTD_PUBLIC_ID, DTD_RESOURCE_NAME);
	map.put(DTD_PUBLIC_CS_ID, DTD_RESOURCE_NAME);
	return map;
    }

}

