class Node {
    /***************************************************************************
    * Method to remove the child at the specified index from this node's 
    * children and sets that node's parent to null. 
    * @param childIndex the index of the this node's children to be removed
    ***************************************************************************/
    public void remove(int childIndex) {
	if (childIndex &gt; -1 && childIndex &lt; children.size()) {
	    Node child = (Node) children.items[childIndex];
	    child.setParent(null);
	    children.del(childIndex);
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

