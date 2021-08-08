import com.sun.org.apache.xml.internal.res.XMLErrorResources;
import com.sun.org.apache.xml.internal.res.XMLMessages;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

class DOMBuilder implements ContentHandler, LexicalHandler {
    /**
    * Append a node to the current container.
    *
    * @param newNode New node to append
    */
    protected void append(Node newNode) throws org.xml.sax.SAXException {

	Node currentNode = m_currentNode;

	if (null != currentNode) {
	    if (currentNode == m_root && m_nextSibling != null)
		currentNode.insertBefore(newNode, m_nextSibling);
	    else
		currentNode.appendChild(newNode);

	    // System.out.println(newNode.getNodeName());
	} else if (null != m_docFrag) {
	    if (m_nextSibling != null)
		m_docFrag.insertBefore(newNode, m_nextSibling);
	    else
		m_docFrag.appendChild(newNode);
	} else {
	    boolean ok = true;
	    short type = newNode.getNodeType();

	    if (type == Node.TEXT_NODE) {
		String data = newNode.getNodeValue();

		if ((null != data) && (data.trim().length() &gt; 0)) {
		    throw new org.xml.sax.SAXException(
			    XMLMessages.createXMLMessage(XMLErrorResources.ER_CANT_OUTPUT_TEXT_BEFORE_DOC, null)); //"Warning: can't output text before document element!  Ignoring...");
		}

		ok = false;
	    } else if (type == Node.ELEMENT_NODE) {
		if (m_doc.getDocumentElement() != null) {
		    ok = false;

		    throw new org.xml.sax.SAXException(
			    XMLMessages.createXMLMessage(XMLErrorResources.ER_CANT_HAVE_MORE_THAN_ONE_ROOT, null)); //"Can't have more than one root on a DOM!");
		}
	    }

	    if (ok) {
		if (m_nextSibling != null)
		    m_doc.insertBefore(newNode, m_nextSibling);
		else
		    m_doc.appendChild(newNode);
	    }
	}
    }

    /** Current node           */
    protected Node m_currentNode = null;
    /** The root node          */
    protected Node m_root = null;
    /** The next sibling node  */
    protected Node m_nextSibling = null;
    /** First node of document fragment or null if not a DocumentFragment     */
    public DocumentFragment m_docFrag = null;
    /** Root document          */
    public Document m_doc;

}

