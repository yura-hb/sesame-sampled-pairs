class Node {
    /***************************************************************************
    * Method to remove aChild from this node's child array, giving it a null parent. 
    * @param aChild the child node to remove.
    ***************************************************************************/
    public void remove(Node aChild) {
	for (int i = 0; i &lt; children.size(); i++) {
	    if (aChild == (Node) children.items[i]) {
		aChild.setParent(null);
		children.del(i);
		return;
	    }
	}
    }

    protected Vector children = null;
    protected Node parent = null;

    /***************************************************************************
    * Method to sets this node's parent to newParent but does not change the 
    * parent's child array. 
    * @param parent the newParent node
    ***************************************************************************/
    public void setParent(Node parent) {
	this.parent = parent;
    }

}

