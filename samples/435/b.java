class DefaultMutableTreeNode implements Cloneable, MutableTreeNode, Serializable {
    /**
     * Returns the root of the tree that contains this node.  The root is
     * the ancestor with a null parent.
     *
     * @see     #isNodeAncestor
     * @return  the root of the tree that contains this node
     */
    public TreeNode getRoot() {
	TreeNode ancestor = this;
	TreeNode previous;

	do {
	    previous = ancestor;
	    ancestor = ancestor.getParent();
	} while (ancestor != null);

	return previous;
    }

}

