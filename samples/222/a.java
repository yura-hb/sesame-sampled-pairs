class Node {
    /***************************************************************************
    * Method to remove all of this node's children, setting their parents to null. 
    ***************************************************************************/
    public void removeAllChildren() {
	for (int i = 0; i &lt; children.size(); i++) {
	    Node node = (Node) children.items[i];
	    node.setParent(null);
	}
	children.clear();
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

