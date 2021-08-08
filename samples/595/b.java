import com.sun.org.apache.xml.internal.utils.AttList;
import com.sun.org.apache.xml.internal.utils.DOM2Helper;
import javax.xml.transform.Result;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.LocatorImpl;

class TreeWalker {
    /**
    * Start processing given node
    *
    *
    * @param node Node to process
    *
    * @throws org.xml.sax.SAXException
    */
    protected void startNode(Node node) throws org.xml.sax.SAXException {

	//   TODO: &lt;REVIEW&gt;
	//    A Serializer implements ContentHandler, but not NodeConsumer
	//    so drop this reference to NodeConsumer which would otherwise
	//    pull in all sorts of things
	//    if (m_contentHandler instanceof NodeConsumer)
	//    {
	//      ((NodeConsumer) m_contentHandler).setOriginatingNode(node);
	//    }
	//    TODO: &lt;/REVIEW&gt;

	if (node instanceof Locator) {
	    Locator loc = (Locator) node;
	    m_locator.setColumnNumber(loc.getColumnNumber());
	    m_locator.setLineNumber(loc.getLineNumber());
	    m_locator.setPublicId(loc.getPublicId());
	    m_locator.setSystemId(loc.getSystemId());
	} else {
	    m_locator.setColumnNumber(0);
	    m_locator.setLineNumber(0);
	}

	switch (node.getNodeType()) {
	case Node.COMMENT_NODE: {
	    String data = ((Comment) node).getData();

	    if (m_contentHandler instanceof LexicalHandler) {
		LexicalHandler lh = ((LexicalHandler) this.m_contentHandler);

		lh.comment(data.toCharArray(), 0, data.length());
	    }
	}
	    break;
	case Node.DOCUMENT_FRAGMENT_NODE:

	    // ??;
	    break;
	case Node.DOCUMENT_NODE:

	    break;
	case Node.ELEMENT_NODE:
	    Element elem_node = (Element) node; {
	    // Make sure the namespace node
	    // for the element itself is declared
	    // to the ContentHandler
	    String uri = elem_node.getNamespaceURI();
	    if (uri != null) {
		String prefix = elem_node.getPrefix();
		if (prefix == null)
		    prefix = "";
		this.m_contentHandler.startPrefixMapping(prefix, uri);
	    }
	}
	    NamedNodeMap atts = elem_node.getAttributes();
	    int nAttrs = atts.getLength();
	    // System.out.println("TreeWalker#startNode: "+node.getNodeName());

	    // Make sure the namespace node of
	    // each attribute is declared to the ContentHandler
	    for (int i = 0; i &lt; nAttrs; i++) {
		final Node attr = atts.item(i);
		final String attrName = attr.getNodeName();
		final int colon = attrName.indexOf(':');
		final String prefix;

		if (attrName.equals("xmlns") || attrName.startsWith("xmlns:")) {
		    // Use "" instead of null, as Xerces likes "" for the
		    // name of the default namespace.  Fix attributed
		    // to "Steven Murray" &lt;smurray@ebt.com&gt;.
		    if (colon &lt; 0)
			prefix = "";
		    else
			prefix = attrName.substring(colon + 1);

		    this.m_contentHandler.startPrefixMapping(prefix, attr.getNodeValue());
		} else if (colon &gt; 0) {
		    prefix = attrName.substring(0, colon);
		    String uri = attr.getNamespaceURI();
		    if (uri != null)
			this.m_contentHandler.startPrefixMapping(prefix, uri);
		}
	    }

	    String ns = DOM2Helper.getNamespaceOfNode(node);
	    if (null == ns)
		ns = "";
	    this.m_contentHandler.startElement(ns, DOM2Helper.getLocalNameOfNode(node), node.getNodeName(),
		    new AttList(atts));
	    break;
	case Node.PROCESSING_INSTRUCTION_NODE: {
	    ProcessingInstruction pi = (ProcessingInstruction) node;
	    String name = pi.getNodeName();

	    // String data = pi.getData();
	    if (name.equals("xslt-next-is-raw")) {
		nextIsRaw = true;
	    } else {
		this.m_contentHandler.processingInstruction(pi.getNodeName(), pi.getData());
	    }
	}
	    break;
	case Node.CDATA_SECTION_NODE: {
	    boolean isLexH = (m_contentHandler instanceof LexicalHandler);
	    LexicalHandler lh = isLexH ? ((LexicalHandler) this.m_contentHandler) : null;

	    if (isLexH) {
		lh.startCDATA();
	    }

	    dispatachChars(node);

	    {
		if (isLexH) {
		    lh.endCDATA();
		}
	    }
	}
	    break;
	case Node.TEXT_NODE: {
	    //String data = ((Text) node).getData();

	    if (nextIsRaw) {
		nextIsRaw = false;

		m_contentHandler.processingInstruction(Result.PI_DISABLE_OUTPUT_ESCAPING, "");
		dispatachChars(node);
		m_contentHandler.processingInstruction(Result.PI_ENABLE_OUTPUT_ESCAPING, "");
	    } else {
		dispatachChars(node);
	    }
	}
	    break;
	case Node.ENTITY_REFERENCE_NODE: {
	    EntityReference eref = (EntityReference) node;

	    if (m_contentHandler instanceof LexicalHandler) {
		((LexicalHandler) this.m_contentHandler).startEntity(eref.getNodeName());
	    } else {

		// warning("Can not output entity to a pure SAX ContentHandler");
	    }
	}
	    break;
	default:
	}
    }

    /** Locator object for this TreeWalker          */
    final private LocatorImpl m_locator = new LocatorImpl();
    /** Local reference to a ContentHandler          */
    final private ContentHandler m_contentHandler;
    boolean nextIsRaw = false;
    /**
    * If m_contentHandler is a SerializationHandler, then this is
    * a reference to the same object.
    */
    final private SerializationHandler m_Serializer;

    /**
    * Optimized dispatch of characters.
    */
    private final void dispatachChars(Node node) throws org.xml.sax.SAXException {
	if (m_Serializer != null) {
	    this.m_Serializer.characters(node);
	} else {
	    String data = ((Text) node).getData();
	    this.m_contentHandler.characters(data.toCharArray(), 0, data.length());
	}
    }

}

