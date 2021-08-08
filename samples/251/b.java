import java.util.List;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

class ToUnknownStream extends SerializerBase {
    /**
     * Pass the call on to the underlying handler
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
	if (m_firstTagNotEmitted) {
	    flush();
	    if (namespaceURI == null && m_firstElementURI != null)
		namespaceURI = m_firstElementURI;

	    if (localName == null && m_firstElementLocalName != null)
		localName = m_firstElementLocalName;
	}
	m_handler.endElement(namespaceURI, localName, qName);
    }

    /**
     * true if the first tag has been emitted to the wrapped handler
     */
    private boolean m_firstTagNotEmitted = true;
    /**
     * the namespace URI associated with the first element
     */
    private String m_firstElementURI;
    /**
     * the local name (no prefix) associated with the first element
     */
    private String m_firstElementLocalName = null;
    /**
     * The wrapped handler, initially XML but possibly switched to HTML
     */
    private SerializationHandler m_handler;
    /**
     * true if startDocument() was called before the underlying handler
     * was initialized
     */
    private boolean m_needToCallStartDocument = false;
    /**
     * the element name (including any prefix) of the very first tag in the document
     */
    private String m_firstElementName;
    /**
     * true if the underlying handler (XML or HTML) is fully initialized
     */
    private boolean m_wrapped_handler_not_initialized = false;
    /**
     * A collection of namespace Prefix (only for first element)
     * _namespaceURI has the matching URIs for these prefix'
     */
    private List&lt;String&gt; m_namespacePrefix = null;
    /**
     * A collection of namespace URI's (only for first element).
     * _namespacePrefix has the matching prefix for these URI's
     */
    private List&lt;String&gt; m_namespaceURI = null;
    /**
     * A String with no characters
     */
    private static final String EMPTYSTRING = "";
    /**
     * the prefix of the very first tag in the document
     */
    private String m_firstElementPrefix;

    private void flush() {
	try {
	    if (m_firstTagNotEmitted) {
		emitFirstTag();
	    }
	    if (m_needToCallStartDocument) {
		m_handler.startDocument();
		m_needToCallStartDocument = false;
	    }
	} catch (SAXException e) {
	    throw new RuntimeException(e.toString());
	}
    }

    private void emitFirstTag() throws SAXException {
	if (m_firstElementName != null) {
	    if (m_wrapped_handler_not_initialized) {
		initStreamOutput();
		m_wrapped_handler_not_initialized = false;
	    }
	    // Output first tag
	    m_handler.startElement(m_firstElementURI, null, m_firstElementName, m_attributes);
	    // don't need the collected attributes of the first element anymore.
	    m_attributes = null;

	    // Output namespaces of first tag
	    if (m_namespacePrefix != null) {
		final int n = m_namespacePrefix.size();
		for (int i = 0; i &lt; n; i++) {
		    final String prefix = m_namespacePrefix.get(i);
		    final String uri = m_namespaceURI.get(i);
		    m_handler.startPrefixMapping(prefix, uri, false);
		}
		m_namespacePrefix = null;
		m_namespaceURI = null;
	    }
	    m_firstTagNotEmitted = false;
	}
    }

    /**
     * Initialize the wrapped output stream (XML or HTML).
     * If the stream handler should be HTML, then replace the XML handler with
     * an HTML handler. After than send the starting method calls that were cached
     * to the wrapped handler.
     *
     */
    private void initStreamOutput() throws SAXException {

	// Try to rule out if this is an not to be an HTML document based on prefix
	boolean firstElementIsHTML = isFirstElemHTML();

	if (firstElementIsHTML) {
	    // create an HTML output handler, and initialize it

	    // keep a reference to the old handler, ... it will soon be gone
	    SerializationHandler oldHandler = m_handler;

	    /* We have to make sure we get an output properties with the proper
	     * defaults for the HTML method.  The easiest way to do this is to
	     * have the OutputProperties class do it.
	     */

	    Properties htmlProperties = OutputPropertiesFactory.getDefaultMethodProperties(Method.HTML);
	    Serializer serializer = SerializerFactory.getSerializer(htmlProperties);

	    // The factory should be returning a ToStream
	    // Don't know what to do if it doesn't
	    // i.e. the user has over-ridden the content-handler property
	    // for html
	    m_handler = (SerializationHandler) serializer;
	    //m_handler = new ToHTMLStream();

	    Writer writer = oldHandler.getWriter();

	    if (null != writer)
		m_handler.setWriter(writer);
	    else {
		OutputStream os = oldHandler.getOutputStream();

		if (null != os)
		    m_handler.setOutputStream(os);
	    }

	    // need to copy things from the old handler to the new one here

	    //            if (_setVersion_called)
	    //            {
	    m_handler.setVersion(oldHandler.getVersion());
	    //            }
	    //            if (_setDoctypeSystem_called)
	    //            {
	    m_handler.setDoctypeSystem(oldHandler.getDoctypeSystem());
	    //            }
	    //            if (_setDoctypePublic_called)
	    //            {
	    m_handler.setDoctypePublic(oldHandler.getDoctypePublic());
	    //            }
	    //            if (_setMediaType_called)
	    //            {
	    m_handler.setMediaType(oldHandler.getMediaType());
	    //            }

	    m_handler.setTransformer(oldHandler.getTransformer());
	}

	/* Now that we have a real wrapped handler (XML or HTML) lets
	 * pass any cached calls to it
	 */
	// Call startDocument() if necessary
	if (m_needToCallStartDocument) {
	    m_handler.startDocument();
	    m_needToCallStartDocument = false;
	}

	// the wrapped handler is now fully initialized
	m_wrapped_handler_not_initialized = false;
    }

    /**
     * Determine if the firts element in the document is &lt;html&gt; or &lt;HTML&gt;
     * This uses the cached first element name, first element prefix and the
     * cached namespaces from previous method calls
     *
     * @return true if the first element is an opening &lt;html&gt; tag
     */
    private boolean isFirstElemHTML() {
	boolean isHTML;

	// is the first tag html, not considering the prefix ?
	isHTML = getLocalNameUnknown(m_firstElementName).equalsIgnoreCase("html");

	// Try to rule out if this is not to be an HTML document based on URI
	if (isHTML && m_firstElementURI != null && !EMPTYSTRING.equals(m_firstElementURI)) {
	    // the &lt;html&gt; element has a non-trivial namespace
	    isHTML = false;
	}
	// Try to rule out if this is an not to be an HTML document based on prefix
	if (isHTML && m_namespacePrefix != null) {
	    /* the first element has a name of "html", but lets check the prefix.
	     * If the prefix points to a namespace with a URL that is not ""
	     * then the doecument doesn't start with an &lt;html&gt; tag, and isn't html
	     */
	    final int max = m_namespacePrefix.size();
	    for (int i = 0; i &lt; max; i++) {
		final String prefix = m_namespacePrefix.get(i);
		final String uri = m_namespaceURI.get(i);

		if (m_firstElementPrefix != null && m_firstElementPrefix.equals(prefix) && !EMPTYSTRING.equals(uri)) {
		    // The first element has a prefix, so it can't be &lt;html&gt;
		    isHTML = false;
		    break;
		}
	    }

	}
	return isHTML;
    }

    /**
     * Utility function for calls to local-name().
     *
     * Don't want to override static function on SerializerBase
     * So added Unknown suffix to method name.
     */
    private String getLocalNameUnknown(String value) {
	int idx = value.lastIndexOf(':');
	if (idx &gt;= 0)
	    value = value.substring(idx + 1);
	idx = value.lastIndexOf('@');
	if (idx &gt;= 0)
	    value = value.substring(idx + 1);
	return (value);
    }

}

