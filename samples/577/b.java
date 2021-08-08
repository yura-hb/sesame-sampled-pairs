import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xml.internal.serializer.NamespaceMappings;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import org.xml.sax.SAXException;

class BasisLibrary {
    /**
     * Utility function for the implementation of xsl:element.
     */
    public static String startXslElement(String qname, String namespace, SerializationHandler handler, DOM dom,
	    int node) {
	try {
	    // Get prefix from qname
	    String prefix;
	    final int index = qname.indexOf(':');

	    if (index &gt; 0) {
		prefix = qname.substring(0, index);

		// Handle case when prefix is not known at compile time
		if (namespace == null || namespace.length() == 0) {
		    try {
			// not sure if this line of code ever works
			namespace = dom.lookupNamespace(node, prefix);
		    } catch (RuntimeException e) {
			handler.flushPending(); // need to flush or else can't get namespacemappings
			NamespaceMappings nm = handler.getNamespaceMappings();
			namespace = nm.lookupNamespace(prefix);
			if (namespace == null) {
			    runTimeError(NAMESPACE_PREFIX_ERR, prefix);
			}
		    }
		}

		handler.startElement(namespace, qname.substring(index + 1), qname);
		handler.namespaceAfterStartElement(prefix, namespace);
	    } else {
		// Need to generate a prefix?
		if (namespace != null && namespace.length() &gt; 0) {
		    prefix = generatePrefix();
		    qname = prefix + ':' + qname;
		    handler.startElement(namespace, qname, qname);
		    handler.namespaceAfterStartElement(prefix, namespace);
		} else {
		    handler.startElement(null, null, qname);
		}
	    }
	} catch (SAXException e) {
	    throw new RuntimeException(e.getMessage());
	}

	return qname;
    }

    public static final String NAMESPACE_PREFIX_ERR = "NAMESPACE_PREFIX_ERR";
    private static final ThreadLocal&lt;AtomicInteger&gt; threadLocalPrefixIndex = new ThreadLocal&lt;AtomicInteger&gt;() {
	@Override
	protected AtomicInteger initialValue() {
	    return new AtomicInteger();
	}
    };
    private static ResourceBundle m_bundle;

    public static void runTimeError(String code, Object arg0) {
	runTimeError(code, new Object[] { arg0 });
    }

    /**
     * These functions are used in the execution of xsl:element to generate
     * and reset namespace prefix index local to current transformation process
     */
    public static String generatePrefix() {
	return ("ns" + threadLocalPrefixIndex.get().getAndIncrement());
    }

    public static void runTimeError(String code, Object[] args) {
	final String message = MessageFormat.format(m_bundle.getString(code), args);
	throw new RuntimeException(message);
    }

}

