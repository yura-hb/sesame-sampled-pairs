import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

class TimeZoneNameUtility {
    /**
     * Retrieves a generic time zone display name for a time zone ID.
     *
     * @param id     time zone ID
     * @param style  TimeZone.LONG or TimeZone.SHORT
     * @param locale desired Locale
     * @return the requested generic time zone display name, or null if not found.
     */
    public static String retrieveGenericDisplayName(String id, int style, Locale locale) {
	String[] names = retrieveDisplayNamesImpl(id, locale);
	if (Objects.nonNull(names)) {
	    return names[6 - style];
	} else {
	    return null;
	}
    }

    /**
     * Cache for managing display names per timezone per locale
     * The structure is:
     *     Map(key=id, value=SoftReference(Map(key=locale, value=displaynames)))
     */
    private static final Map&lt;String, SoftReference&lt;Map&lt;Locale, String[]&gt;&gt;&gt; cachedDisplayNames = new ConcurrentHashMap&lt;&gt;();

    private static String[] retrieveDisplayNamesImpl(String id, Locale locale) {
	LocaleServiceProviderPool pool = LocaleServiceProviderPool.getPool(TimeZoneNameProvider.class);
	String[] names;
	Map&lt;Locale, String[]&gt; perLocale = null;

	SoftReference&lt;Map&lt;Locale, String[]&gt;&gt; ref = cachedDisplayNames.get(id);
	if (Objects.nonNull(ref)) {
	    perLocale = ref.get();
	    if (Objects.nonNull(perLocale)) {
		names = perLocale.get(locale);
		if (Objects.nonNull(names)) {
		    return names;
		}
	    }
	}

	// build names array
	names = new String[7];
	names[0] = id;
	for (int i = 1; i &lt;= 6; i++) {
	    names[i] = pool.getLocalizedObject(TimeZoneNameGetter.INSTANCE, locale,
		    i &lt; 5 ? (i &lt; 3 ? "std" : "dst") : "generic", i % 2, id);
	}

	if (Objects.isNull(perLocale)) {
	    perLocale = new ConcurrentHashMap&lt;&gt;();
	}
	perLocale.put(locale, names);
	ref = new SoftReference&lt;&gt;(perLocale);
	cachedDisplayNames.put(id, ref);
	return names;
    }

}

