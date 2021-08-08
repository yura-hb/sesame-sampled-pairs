import java.util.*;
import static java.util.Locale.ENGLISH;

abstract class Provider extends Properties {
    /**
     * Get an unmodifiable Set of all services supported by
     * this Provider.
     *
     * @return an unmodifiable Set of all services supported by
     * this Provider
     *
     * @since 1.5
     */
    public synchronized Set&lt;Service&gt; getServices() {
	checkInitialized();
	if (legacyChanged || servicesChanged) {
	    serviceSet = null;
	}
	if (serviceSet == null) {
	    ensureLegacyParsed();
	    Set&lt;Service&gt; set = new LinkedHashSet&lt;&gt;();
	    if (serviceMap != null) {
		set.addAll(serviceMap.values());
	    }
	    if (legacyMap != null) {
		set.addAll(legacyMap.values());
	    }
	    serviceSet = Collections.unmodifiableSet(set);
	    servicesChanged = false;
	}
	return serviceSet;
    }

    private transient boolean legacyChanged;
    private transient boolean servicesChanged;
    private transient Set&lt;Service&gt; serviceSet;
    private transient Map&lt;ServiceKey, Service&gt; serviceMap;
    private transient Map&lt;ServiceKey, Service&gt; legacyMap;
    private transient boolean initialized;
    private transient Map&lt;String, String&gt; legacyStrings;
    private static final String ALIAS_PREFIX_LOWER = "alg.alias.";
    private static final int ALIAS_LENGTH = ALIAS_PREFIX.length();
    private static final sun.security.util.Debug debug = sun.security.util.Debug.getInstance("provider", "Provider");
    /**
     * The provider name.
     *
     * @serial
     */
    private String name;
    private static final Map&lt;String, EngineDescription&gt; knownEngines;

    private void checkInitialized() {
	if (!initialized) {
	    throw new IllegalStateException();
	}
    }

    /**
     * Ensure all the legacy String properties are fully parsed into
     * service objects.
     */
    private void ensureLegacyParsed() {
	if ((legacyChanged == false) || (legacyStrings == null)) {
	    return;
	}
	serviceSet = null;
	if (legacyMap == null) {
	    legacyMap = new LinkedHashMap&lt;&gt;();
	} else {
	    legacyMap.clear();
	}
	for (Map.Entry&lt;String, String&gt; entry : legacyStrings.entrySet()) {
	    parseLegacyPut(entry.getKey(), entry.getValue());
	}
	removeInvalidServices(legacyMap);
	legacyChanged = false;
    }

    private void parseLegacyPut(String name, String value) {
	if (name.toLowerCase(ENGLISH).startsWith(ALIAS_PREFIX_LOWER)) {
	    // e.g. put("Alg.Alias.MessageDigest.SHA", "SHA-1");
	    // aliasKey ~ MessageDigest.SHA
	    String stdAlg = value;
	    String aliasKey = name.substring(ALIAS_LENGTH);
	    String[] typeAndAlg = getTypeAndAlgorithm(aliasKey);
	    if (typeAndAlg == null) {
		return;
	    }
	    String type = getEngineName(typeAndAlg[0]);
	    String aliasAlg = typeAndAlg[1].intern();
	    ServiceKey key = new ServiceKey(type, stdAlg, true);
	    Service s = legacyMap.get(key);
	    if (s == null) {
		s = new Service(this);
		s.type = type;
		s.algorithm = stdAlg;
		legacyMap.put(key, s);
	    }
	    legacyMap.put(new ServiceKey(type, aliasAlg, true), s);
	    s.addAlias(aliasAlg);
	} else {
	    String[] typeAndAlg = getTypeAndAlgorithm(name);
	    if (typeAndAlg == null) {
		return;
	    }
	    int i = typeAndAlg[1].indexOf(' ');
	    if (i == -1) {
		// e.g. put("MessageDigest.SHA-1", "sun.security.provider.SHA");
		String type = getEngineName(typeAndAlg[0]);
		String stdAlg = typeAndAlg[1].intern();
		String className = value;
		ServiceKey key = new ServiceKey(type, stdAlg, true);
		Service s = legacyMap.get(key);
		if (s == null) {
		    s = new Service(this);
		    s.type = type;
		    s.algorithm = stdAlg;
		    legacyMap.put(key, s);
		}
		s.className = className;
	    } else { // attribute
		// e.g. put("MessageDigest.SHA-1 ImplementedIn", "Software");
		String attributeValue = value;
		String type = getEngineName(typeAndAlg[0]);
		String attributeString = typeAndAlg[1];
		String stdAlg = attributeString.substring(0, i).intern();
		String attributeName = attributeString.substring(i + 1);
		// kill additional spaces
		while (attributeName.startsWith(" ")) {
		    attributeName = attributeName.substring(1);
		}
		attributeName = attributeName.intern();
		ServiceKey key = new ServiceKey(type, stdAlg, true);
		Service s = legacyMap.get(key);
		if (s == null) {
		    s = new Service(this);
		    s.type = type;
		    s.algorithm = stdAlg;
		    legacyMap.put(key, s);
		}
		s.addAttribute(attributeName, attributeValue);
	    }
	}
    }

    /**
     * Remove all invalid services from the Map. Invalid services can only
     * occur if the legacy properties are inconsistent or incomplete.
     */
    private void removeInvalidServices(Map&lt;ServiceKey, Service&gt; map) {
	for (Iterator&lt;Map.Entry&lt;ServiceKey, Service&gt;&gt; t = map.entrySet().iterator(); t.hasNext();) {
	    Service s = t.next().getValue();
	    if (s.isValid() == false) {
		t.remove();
	    }
	}
    }

    private String[] getTypeAndAlgorithm(String key) {
	int i = key.indexOf('.');
	if (i &lt; 1) {
	    if (debug != null) {
		debug.println("Ignoring invalid entry in provider " + name + ":" + key);
	    }
	    return null;
	}
	String type = key.substring(0, i);
	String alg = key.substring(i + 1);
	return new String[] { type, alg };
    }

    private static String getEngineName(String s) {
	// try original case first, usually correct
	EngineDescription e = knownEngines.get(s);
	if (e == null) {
	    e = knownEngines.get(s.toLowerCase(ENGLISH));
	}
	return (e == null) ? s : e.name;
    }

    class ServiceKey {
	private transient boolean legacyChanged;
	private transient boolean servicesChanged;
	private transient Set&lt;Service&gt; serviceSet;
	private transient Map&lt;ServiceKey, Service&gt; serviceMap;
	private transient Map&lt;ServiceKey, Service&gt; legacyMap;
	private transient boolean initialized;
	private transient Map&lt;String, String&gt; legacyStrings;
	private static final String ALIAS_PREFIX_LOWER = "alg.alias.";
	private static final int ALIAS_LENGTH = ALIAS_PREFIX.length();
	private static final sun.security.util.Debug debug = sun.security.util.Debug.getInstance("provider",
		"Provider");
	/**
	* The provider name.
	*
	* @serial
	*/
	private String name;
	private static final Map&lt;String, EngineDescription&gt; knownEngines;

	private ServiceKey(String type, String algorithm, boolean intern) {
	    this.type = type;
	    this.originalAlgorithm = algorithm;
	    algorithm = algorithm.toUpperCase(ENGLISH);
	    this.algorithm = intern ? algorithm.intern() : algorithm;
	}

    }

    class Service {
	private transient boolean legacyChanged;
	private transient boolean servicesChanged;
	private transient Set&lt;Service&gt; serviceSet;
	private transient Map&lt;ServiceKey, Service&gt; serviceMap;
	private transient Map&lt;ServiceKey, Service&gt; legacyMap;
	private transient boolean initialized;
	private transient Map&lt;String, String&gt; legacyStrings;
	private static final String ALIAS_PREFIX_LOWER = "alg.alias.";
	private static final int ALIAS_LENGTH = ALIAS_PREFIX.length();
	private static final sun.security.util.Debug debug = sun.security.util.Debug.getInstance("provider",
		"Provider");
	/**
	* The provider name.
	*
	* @serial
	*/
	private String name;
	private static final Map&lt;String, EngineDescription&gt; knownEngines;

	private Service(Provider provider) {
	    this.provider = provider;
	    aliases = Collections.&lt;String&gt;emptyList();
	    attributes = Collections.&lt;UString, String&gt;emptyMap();
	}

	private void addAlias(String alias) {
	    if (aliases.isEmpty()) {
		aliases = new ArrayList&lt;&gt;(2);
	    }
	    aliases.add(alias);
	}

	void addAttribute(String type, String value) {
	    if (attributes.isEmpty()) {
		attributes = new HashMap&lt;&gt;(8);
	    }
	    attributes.put(new UString(type), value);
	}

	private boolean isValid() {
	    return (type != null) && (algorithm != null) && (className != null);
	}

    }

    class UString {
	private transient boolean legacyChanged;
	private transient boolean servicesChanged;
	private transient Set&lt;Service&gt; serviceSet;
	private transient Map&lt;ServiceKey, Service&gt; serviceMap;
	private transient Map&lt;ServiceKey, Service&gt; legacyMap;
	private transient boolean initialized;
	private transient Map&lt;String, String&gt; legacyStrings;
	private static final String ALIAS_PREFIX_LOWER = "alg.alias.";
	private static final int ALIAS_LENGTH = ALIAS_PREFIX.length();
	private static final sun.security.util.Debug debug = sun.security.util.Debug.getInstance("provider",
		"Provider");
	/**
	* The provider name.
	*
	* @serial
	*/
	private String name;
	private static final Map&lt;String, EngineDescription&gt; knownEngines;

	UString(String s) {
	    this.string = s;
	    this.lowerString = s.toLowerCase(ENGLISH);
	}

    }

}

