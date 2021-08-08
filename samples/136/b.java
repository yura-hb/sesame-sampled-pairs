import java.util.Map;

abstract class LocaleProviderAdapter {
    /**
     * Returns the singleton instance for each adapter type
     */
    public static LocaleProviderAdapter forType(Type type) {
	switch (type) {
	case JRE:
	case CLDR:
	case SPI:
	case HOST:
	case FALLBACK:
	    LocaleProviderAdapter adapter = null;
	    LocaleProviderAdapter cached = adapterInstances.get(type);
	    if (cached == null) {
		try {
		    // lazily load adapters here
		    @SuppressWarnings("deprecation")
		    Object tmp = Class.forName(type.getAdapterClassName()).newInstance();
		    adapter = (LocaleProviderAdapter) tmp;
		    cached = adapterInstances.putIfAbsent(type, adapter);
		    if (cached != null) {
			adapter = cached;
		    }
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException
			| UnsupportedOperationException e) {
		    LocaleServiceProviderPool.config(LocaleProviderAdapter.class, e.toString());
		    adapterInstances.putIfAbsent(type, NONEXISTENT_ADAPTER);
		    if (defaultLocaleProviderAdapter == type) {
			defaultLocaleProviderAdapter = Type.FALLBACK;
		    }
		}
	    } else if (cached != NONEXISTENT_ADAPTER) {
		adapter = cached;
	    }
	    return adapter;
	default:
	    throw new InternalError("unknown locale data adapter type");
	}
    }

    /**
     * LocaleProviderAdapter instances
     */
    private static final Map&lt;Type, LocaleProviderAdapter&gt; adapterInstances = new ConcurrentHashMap&lt;&gt;();
    private static final LocaleProviderAdapter NONEXISTENT_ADAPTER = new NonExistentAdapter();
    /**
     * Default fallback adapter type, which should return something meaningful in any case.
     * This is either CLDR or FALLBACK.
     */
    static volatile LocaleProviderAdapter.Type defaultLocaleProviderAdapter;

    class Type extends Enum&lt;Type&gt; {
	/**
	* LocaleProviderAdapter instances
	*/
	private static final Map&lt;Type, LocaleProviderAdapter&gt; adapterInstances = new ConcurrentHashMap&lt;&gt;();
	private static final LocaleProviderAdapter NONEXISTENT_ADAPTER = new NonExistentAdapter();
	/**
	* Default fallback adapter type, which should return something meaningful in any case.
	* This is either CLDR or FALLBACK.
	*/
	static volatile LocaleProviderAdapter.Type defaultLocaleProviderAdapter;

	public String getAdapterClassName() {
	    return CLASSNAME;
	}

    }

}

