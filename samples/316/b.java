import jdk.internal.misc.VM;
import java.nio.charset.spi.CharsetProvider;
import java.security.AccessController;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

abstract class Charset implements Comparable&lt;Charset&gt; {
    /**
     * Tells whether the named charset is supported.
     *
     * @param  charsetName
     *         The name of the requested charset; may be either
     *         a canonical name or an alias
     *
     * @return  {@code true} if, and only if, support for the named charset
     *          is available in the current Java virtual machine
     *
     * @throws IllegalCharsetNameException
     *         If the given charset name is illegal
     *
     * @throws  IllegalArgumentException
     *          If the given {@code charsetName} is null
     */
    public static boolean isSupported(String charsetName) {
	return (lookup(charsetName) != null);
    }

    private static volatile Object[] cache1;
    private static volatile Object[] cache2;
    private static final CharsetProvider standardProvider = new sun.nio.cs.StandardCharsets();
    private static ThreadLocal&lt;ThreadLocal&lt;?&gt;&gt; gate = new ThreadLocal&lt;ThreadLocal&lt;?&gt;&gt;();

    private static Charset lookup(String charsetName) {
	if (charsetName == null)
	    throw new IllegalArgumentException("Null charset name");
	Object[] a;
	if ((a = cache1) != null && charsetName.equals(a[0]))
	    return (Charset) a[1];
	// We expect most programs to use one Charset repeatedly.
	// We convey a hint to this effect to the VM by putting the
	// level 1 cache miss code in a separate method.
	return lookup2(charsetName);
    }

    private static Charset lookup2(String charsetName) {
	Object[] a;
	if ((a = cache2) != null && charsetName.equals(a[0])) {
	    cache2 = cache1;
	    cache1 = a;
	    return (Charset) a[1];
	}
	Charset cs;
	if ((cs = standardProvider.charsetForName(charsetName)) != null
		|| (cs = lookupExtendedCharset(charsetName)) != null
		|| (cs = lookupViaProviders(charsetName)) != null) {
	    cache(charsetName, cs);
	    return cs;
	}

	/* Only need to check the name if we didn't find a charset for it */
	checkName(charsetName);
	return null;
    }

    private static Charset lookupExtendedCharset(String charsetName) {
	if (!VM.isBooted()) // see lookupViaProviders()
	    return null;
	CharsetProvider[] ecps = ExtendedProviderHolder.extendedProviders;
	for (CharsetProvider cp : ecps) {
	    Charset cs = cp.charsetForName(charsetName);
	    if (cs != null)
		return cs;
	}
	return null;
    }

    private static Charset lookupViaProviders(final String charsetName) {

	// The runtime startup sequence looks up standard charsets as a
	// consequence of the VM's invocation of System.initializeSystemClass
	// in order to, e.g., set system properties and encode filenames.  At
	// that point the application class loader has not been initialized,
	// however, so we can't look for providers because doing so will cause
	// that loader to be prematurely initialized with incomplete
	// information.
	//
	if (!VM.isBooted())
	    return null;

	if (gate.get() != null)
	    // Avoid recursive provider lookups
	    return null;
	try {
	    gate.set(gate);

	    return AccessController.doPrivileged(new PrivilegedAction&lt;&gt;() {
		public Charset run() {
		    for (Iterator&lt;CharsetProvider&gt; i = providers(); i.hasNext();) {
			CharsetProvider cp = i.next();
			Charset cs = cp.charsetForName(charsetName);
			if (cs != null)
			    return cs;
		    }
		    return null;
		}
	    });

	} finally {
	    gate.set(null);
	}
    }

    private static void cache(String charsetName, Charset cs) {
	cache2 = cache1;
	cache1 = new Object[] { charsetName, cs };
    }

    /**
     * Checks that the given string is a legal charset name. &lt;/p&gt;
     *
     * @param  s
     *         A purported charset name
     *
     * @throws  IllegalCharsetNameException
     *          If the given name is not a legal charset name
     */
    private static void checkName(String s) {
	int n = s.length();
	if (n == 0) {
	    throw new IllegalCharsetNameException(s);
	}
	for (int i = 0; i &lt; n; i++) {
	    char c = s.charAt(i);
	    if (c &gt;= 'A' && c &lt;= 'Z')
		continue;
	    if (c &gt;= 'a' && c &lt;= 'z')
		continue;
	    if (c &gt;= '0' && c &lt;= '9')
		continue;
	    if (c == '-' && i != 0)
		continue;
	    if (c == '+' && i != 0)
		continue;
	    if (c == ':' && i != 0)
		continue;
	    if (c == '_' && i != 0)
		continue;
	    if (c == '.' && i != 0)
		continue;
	    throw new IllegalCharsetNameException(s);
	}
    }

    private static Iterator&lt;CharsetProvider&gt; providers() {
	return new Iterator&lt;&gt;() {
	    ClassLoader cl = ClassLoader.getSystemClassLoader();
	    ServiceLoader&lt;CharsetProvider&gt; sl = ServiceLoader.load(CharsetProvider.class, cl);
	    Iterator&lt;CharsetProvider&gt; i = sl.iterator();
	    CharsetProvider next = null;

	    private boolean getNext() {
		while (next == null) {
		    try {
			if (!i.hasNext())
			    return false;
			next = i.next();
		    } catch (ServiceConfigurationError sce) {
			if (sce.getCause() instanceof SecurityException) {
			    // Ignore security exceptions
			    continue;
			}
			throw sce;
		    }
		}
		return true;
	    }

	    public boolean hasNext() {
		return getNext();
	    }

	    public CharsetProvider next() {
		if (!getNext())
		    throw new NoSuchElementException();
		CharsetProvider n = next;
		next = null;
		return n;
	    }

	    public void remove() {
		throw new UnsupportedOperationException();
	    }

	};
    }

}

