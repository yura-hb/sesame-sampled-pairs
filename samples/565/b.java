import org.w3c.dom.DOMException;

class TreeWalkerImpl implements TreeWalker {
    /** Return the current Node. */
    public void setCurrentNode(Node node) {
	if (node == null) {
	    String msg = DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, "NOT_SUPPORTED_ERR", null);
	    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
	}

	fCurrentNode = node;
    }

    /** The current Node. */
    Node fCurrentNode;

}

