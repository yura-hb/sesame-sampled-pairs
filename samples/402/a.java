import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

class Messages {
    /**
     * Load the given resource bundle using the specified class loader.
     */
    public static void load(final String bundleName, final ClassLoader loader, final Field[] fields) {
	final String[] variants = buildVariants(bundleName);
	// search the dirs in reverse order so the cascading defaults is set correctly
	for (int i = variants.length; --i &gt;= 0;) {
	    InputStream input = (loader == null) ? ClassLoader.getSystemResourceAsStream(variants[i])
		    : loader.getResourceAsStream(variants[i]);
	    if (input == null)
		continue;
	    try {
		final MessagesProperties properties = new MessagesProperties(fields, bundleName);
		properties.load(input);
	    } catch (IOException e) {
		// ignore
	    } finally {
		try {
		    input.close();
		} catch (IOException e) {
		    // ignore
		}
	    }
	}
    }

    private static String[] nlSuffixes;
    private static final String EXTENSION = ".properties";

    private static String[] buildVariants(String root) {
	if (nlSuffixes == null) {
	    //build list of suffixes for loading resource bundles
	    String nl = Locale.getDefault().toString();
	    ArrayList result = new ArrayList(4);
	    int lastSeparator;
	    while (true) {
		result.add('_' + nl + EXTENSION);
		lastSeparator = nl.lastIndexOf('_');
		if (lastSeparator == -1)
		    break;
		nl = nl.substring(0, lastSeparator);
	    }
	    //add the empty suffix last (most general)
	    result.add(EXTENSION);
	    nlSuffixes = (String[]) result.toArray(new String[result.size()]);
	}
	root = root.replace('.', '/');
	String[] variants = new String[nlSuffixes.length];
	for (int i = 0; i &lt; variants.length; i++)
	    variants[i] = root + nlSuffixes[i];
	return variants;
    }

    class MessagesProperties extends Properties {
	private static String[] nlSuffixes;
	private static final String EXTENSION = ".properties";

	public MessagesProperties(Field[] fieldArray, String bundleName) {
	    super();
	    final int len = fieldArray.length;
	    this.fields = new HashMap(len * 2);
	    for (int i = 0; i &lt; len; i++) {
		this.fields.put(fieldArray[i].getName(), fieldArray[i]);
	    }
	}

    }

}

