class Node {
    /***************************************************************************
    * Method to remove newChild from its parent and makes it a child of this 
    * node by adding it to the end of this node's child vector.
    * @param newChild the new child node to add to the end of the child vector.
    ***************************************************************************/
    public void add(Node newChild) {
	remove(newChild);
	newChild.setParent(this);
	children.add(newChild);
    }

    protected Vector children = null;
    protected Node parent = null;

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

    /***************************************************************************
    * Method to sets this node's parent to newParent but does not change the 
    * parent's child array. 
    * @param parent the newParent node
    ***************************************************************************/
    public void setParent(Node parent) {
	this.parent = parent;
    }

}

