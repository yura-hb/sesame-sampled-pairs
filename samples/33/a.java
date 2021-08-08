class Node {
    /***************************************************************************
    * Method to return the next sibling of this node in the parent's children 
    * array. Returns null if this node has no parent or is the parent's last
    * child. This method performs a linear search that is O(n) where n is the
    * number of children
    * @return the next sibling of this node in the parent's children array. 
    *  Returns null if this node has no parent or is the parent's last child.
    ***************************************************************************/
    public Node getNextSibling() {
	return (parent != null) ? parent.getChildAfter(this) : null;
    }

    protected Node parent = null;
    private static final int AFTER = 1;
    protected Vector children = null;
    private static final int BEFORE = 0;

    /***************************************************************************
    * Method to returns the child in this node's child array that immediately 
    * follows aChild, which must be a child of this node; otherwise, retrun null.  
    * @return the child node that immediately follows aChild node.
    ***************************************************************************/
    public Node getChildAfter(Node aChild) {
	return getChild(aChild, AFTER);
    }

    /***************************************************************************
    * Method to returns the child in this node's child array that immediately 
    * precedes or follows aChild (based on the specified position.  aChild must
    * be a child of this node; otherwise, retrun null.  
    * @return the child node that immediately precede aChild node.
    ***************************************************************************/
    private Node getChild(Node aChild, int position) {
	int pos = children.find(aChild);
	if (position == BEFORE && pos &gt; 0)
	    return (Node) children.items[pos - 1];
	else if (position == AFTER && pos &lt; children.size() - 1)
	    return (Node) children.items[pos + 1];
	return null;
    }

}

