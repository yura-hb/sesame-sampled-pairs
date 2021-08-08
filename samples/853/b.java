import java.util.ResourceBundle;
import java.text.MessageFormat;

class JavacMessages implements Messages {
    /**
     * Returns a localized string from the compiler's default bundle.
     */
    // used to support legacy Log.getLocalizedString
    static String getDefaultLocalizedString(String key, Object... args) {
	return getLocalizedString(List.of(getDefaultBundle()), key, args);
    }

    private static ResourceBundle defaultBundle;
    private static final String defaultBundleName = "com.sun.tools.javac.resources.compiler";

    public static ResourceBundle getDefaultBundle() {
	try {
	    if (defaultBundle == null)
		defaultBundle = ResourceBundle.getBundle(defaultBundleName);
	    return defaultBundle;
	} catch (MissingResourceException e) {
	    throw new Error("Fatal: Resource for compiler is missing", e);
	}
    }

    static private String getLocalizedString(List&lt;ResourceBundle&gt; bundles, String key, Object... args) {
	String msg = null;
	for (List&lt;ResourceBundle&gt; l = bundles; l.nonEmpty() && msg == null; l = l.tail) {
	    ResourceBundle rb = l.head;
	    try {
		msg = rb.getString(key);
	    } catch (MissingResourceException e) {
		// ignore, try other bundles in list
	    }
	}
	if (msg == null) {
	    msg = "compiler message file broken: key=" + key + " arguments={0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}";
	}
	return MessageFormat.format(msg, args);
    }

}

