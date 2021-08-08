abstract class NodeImpl implements Node, NodeList, EventTarget, Cloneable, Serializable {
    /**
     * Returns the node number
     */
    protected int getNodeNumber() {
	int nodeNumber;
	CoreDocumentImpl cd = (CoreDocumentImpl) (this.getOwnerDocument());
	nodeNumber = cd.getNodeNumber(this);
	return nodeNumber;
    }

    protected NodeImpl ownerNode;
    protected short flags;
    protected final static short OWNED = 0x1 &lt;&lt; 3;

    /**
     * Find the Document that this Node belongs to (the document in
     * whose context the Node was created). The Node may or may not
     * currently be part of that Document's actual contents.
     */
    public Document getOwnerDocument() {
	// if we have an owner simply forward the request
	// otherwise ownerNode is our ownerDocument
	if (isOwned()) {
	    return ownerNode.ownerDocument();
	} else {
	    return (Document) ownerNode;
	}
    }

    final boolean isOwned() {
	return (flags & OWNED) != 0;
    }

    /**
     * same as above but returns internal type and this one is not overridden
     * by CoreDocumentImpl to return null
     */
    CoreDocumentImpl ownerDocument() {
	// if we have an owner simply forward the request
	// otherwise ownerNode is our ownerDocument
	if (isOwned()) {
	    return ownerNode.ownerDocument();
	} else {
	    return (CoreDocumentImpl) ownerNode;
	}
    }

}

