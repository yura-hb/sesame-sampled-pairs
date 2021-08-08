import java.util.Locale;
import java.util.Map;

class LocalizedMessage implements Comparable&lt;LocalizedMessage&gt;, Serializable {
    /**
     * Sets a locale to use for localization.
     * @param locale the locale to use for localization
     */
    public static void setLocale(Locale locale) {
	clearCache();
	if (Locale.ENGLISH.getLanguage().equals(locale.getLanguage())) {
	    sLocale = Locale.ROOT;
	} else {
	    sLocale = locale;
	}
    }

    /** The locale to localise messages to. **/
    private static Locale sLocale = Locale.getDefault();
    /**
     * A cache that maps bundle names to ResourceBundles.
     * Avoids repetitive calls to ResourceBundle.getBundle().
     */
    private static final Map&lt;String, ResourceBundle&gt; BUNDLE_CACHE = Collections.synchronizedMap(new HashMap&lt;&gt;());

    /** Clears the cache. */
    public static void clearCache() {
	BUNDLE_CACHE.clear();
    }

}

