class Node {
    /***************************************************************************
    * Method to return the root of the tree that contains this node.
    * @return the root of the tree that contains this node.
    ***************************************************************************/
    public Node getRoot() {
	Node node = this;
	while (!node.isRoot())
	    node = node.getParent();
	return node;
    }

    protected Node parent = null;

    /***************************************************************************
    * Method to return true if this node is a root.  Root node is node that
    * has a null parent node.
    ***************************************************************************/
    public boolean isRoot() {
	return (parent == null);
    }

    /***************************************************************************
    * Method to return this node's parent or null if this node has no parent. 
    ***************************************************************************/
    public Node getParent() {
	return parent;
    }

}

