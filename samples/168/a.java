class Node {
    /***************************************************************************
    * Method to return true if anotherNode is a sibling of (has the same parent
    * as) this node.
    * @return true if anotherNode is a sibling of (has the same parent as) this node.
    ***************************************************************************/
    public boolean isNodeSibling(Node anotherNode) {
	if (parent == null)
	    return false;
	return (parent == anotherNode.parent);
    }

    protected Node parent = null;

}

