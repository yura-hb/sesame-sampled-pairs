import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

class LocalizedMessage implements Comparable&lt;LocalizedMessage&gt;, Serializable {
    /**
     * Gets the translated message.
     * @return the translated message
     */
    public String getMessage() {
	String message = getCustomMessage();

	if (message == null) {
	    try {
		// Important to use the default class loader, and not the one in
		// the GlobalProperties object. This is because the class loader in
		// the GlobalProperties is specified by the user for resolving
		// custom classes.
		final ResourceBundle resourceBundle = getBundle(bundle);
		final String pattern = resourceBundle.getString(key);
		final MessageFormat formatter = new MessageFormat(pattern, Locale.ROOT);
		message = formatter.format(args);
	    } catch (final MissingResourceException ignored) {
		// If the Check author didn't provide i18n resource bundles
		// and logs error messages directly, this will return
		// the author's original message
		final MessageFormat formatter = new MessageFormat(key, Locale.ROOT);
		message = formatter.format(args);
	    }
	}
	return message;
    }

    /** Name of the resource bundle to get messages from. **/
    private final String bundle;
    /** Key for the message format. **/
    private final String key;
    /** Arguments for MessageFormat.
     * @noinspection NonSerializableFieldInSerializableClass
     */
    private final Object[] args;
    /** A custom message overriding the default message from the bundle. */
    private final String customMessage;
    /**
     * A cache that maps bundle names to ResourceBundles.
     * Avoids repetitive calls to ResourceBundle.getBundle().
     */
    private static final Map&lt;String, ResourceBundle&gt; BUNDLE_CACHE = Collections.synchronizedMap(new HashMap&lt;&gt;());
    /** The locale to localise messages to. **/
    private static Locale sLocale = Locale.getDefault();
    /** Class of the source for this LocalizedMessage. */
    private final Class&lt;?&gt; sourceClass;

    /**
     * Returns the formatted custom message if one is configured.
     * @return the formatted custom message or {@code null}
     *          if there is no custom message
     */
    private String getCustomMessage() {
	String message = null;
	if (customMessage != null) {
	    final MessageFormat formatter = new MessageFormat(customMessage, Locale.ROOT);
	    message = formatter.format(args);
	}
	return message;
    }

    /**
     * Find a ResourceBundle for a given bundle name. Uses the classloader
     * of the class emitting this message, to be sure to get the correct
     * bundle.
     * @param bundleName the bundle name
     * @return a ResourceBundle
     */
    private ResourceBundle getBundle(String bundleName) {
	return BUNDLE_CACHE.computeIfAbsent(bundleName,
		name -&gt; ResourceBundle.getBundle(name, sLocale, sourceClass.getClassLoader(), new Utf8Control()));
    }

}

