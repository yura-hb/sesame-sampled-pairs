class Node {
    /***************************************************************************
    * Method to return the number of levels above this node -- the distance 
    * from the root to this node.
    * @return the number of levels above this node -- the distance from the 
    *  root to this node
    ***************************************************************************/
    public int getLevel() {
	int lvl = 0;
	if (this.isRoot())
	    return lvl;

	Node node = getParent();
	lvl++;
	while (!node.isRoot()) {
	    node = node.getParent();
	    lvl++;
	}
	return lvl;
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

