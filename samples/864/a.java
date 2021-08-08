import java.util.TimeZone;
import java.util.concurrent.ConcurrentMap;

class FastDatePrinter implements DatePrinter, Serializable {
    /**
     * &lt;p&gt;Gets the time zone display name, using a cache for performance.&lt;/p&gt;
     *
     * @param tz  the zone to query
     * @param daylight  true if daylight savings
     * @param style  the style to use {@code TimeZone.LONG} or {@code TimeZone.SHORT}
     * @param locale  the locale to use
     * @return the textual name of the time zone
     */
    static String getTimeZoneDisplay(final TimeZone tz, final boolean daylight, final int style, final Locale locale) {
	final TimeZoneDisplayKey key = new TimeZoneDisplayKey(tz, daylight, style, locale);
	String value = cTimeZoneDisplayCache.get(key);
	if (value == null) {
	    // This is a very slow call, so cache the results.
	    value = tz.getDisplayName(daylight, style, locale);
	    final String prior = cTimeZoneDisplayCache.putIfAbsent(key, value);
	    if (prior != null) {
		value = prior;
	    }
	}
	return value;
    }

    private static final ConcurrentMap&lt;TimeZoneDisplayKey, String&gt; cTimeZoneDisplayCache = new ConcurrentHashMap&lt;&gt;(7);

    class TimeZoneDisplayKey {
	private static final ConcurrentMap&lt;TimeZoneDisplayKey, String&gt; cTimeZoneDisplayCache = new ConcurrentHashMap&lt;&gt;(
		7);

	/**
	 * Constructs an instance of {@code TimeZoneDisplayKey} with the specified properties.
	 *
	 * @param timeZone the time zone
	 * @param daylight adjust the style for daylight saving time if {@code true}
	 * @param style the timezone style
	 * @param locale the timezone locale
	 */
	TimeZoneDisplayKey(final TimeZone timeZone, final boolean daylight, final int style, final Locale locale) {
	    mTimeZone = timeZone;
	    if (daylight) {
		mStyle = style | 0x80000000;
	    } else {
		mStyle = style;
	    }
	    mLocale = locale;
	}

    }

}

