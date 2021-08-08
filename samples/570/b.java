import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

class CoreDocumentImpl extends ParentNode implements Document {
    /**
     * Since we cache the docElement (and, currently, docType),
     * replaceChild has to update the cache
     *
     * REVISIT: According to the spec it is not allowed to alter neither the
     * document element nor the document type in any way
     */
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {

	// Adopt orphan doctypes
	if (newChild.getOwnerDocument() == null && newChild instanceof DocumentTypeImpl) {
	    ((DocumentTypeImpl) newChild).ownerDocument = this;
	}

	if (errorChecking && ((docType != null && oldChild.getNodeType() != Node.DOCUMENT_TYPE_NODE
		&& newChild.getNodeType() == Node.DOCUMENT_TYPE_NODE)
		|| (docElement != null && oldChild.getNodeType() != Node.ELEMENT_NODE
			&& newChild.getNodeType() == Node.ELEMENT_NODE))) {

	    throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
		    DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "HIERARCHY_REQUEST_ERR", null));
	}
	super.replaceChild(newChild, oldChild);

	int type = oldChild.getNodeType();
	if (type == Node.ELEMENT_NODE) {
	    docElement = (ElementImpl) newChild;
	} else if (type == Node.DOCUMENT_TYPE_NODE) {
	    docType = (DocumentTypeImpl) newChild;
	}
	return oldChild;
    }

    /** Bypass error checking. */
    protected boolean errorChecking = true;
    /** Document type. */
    protected DocumentTypeImpl docType;
    /** Document element. */
    protected ElementImpl docElement;

}

