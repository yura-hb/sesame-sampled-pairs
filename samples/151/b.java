class DefaultMutableTreeNode implements Cloneable, MutableTreeNode, Serializable {
    /**
     * Returns the number of levels above this node -- the distance from
     * the root to this node.  If this node is the root, returns 0.
     *
     * @see     #getDepth
     * @return  the number of levels above this node
     */
    public int getLevel() {
	TreeNode ancestor;
	int levels = 0;

	ancestor = this;
	while ((ancestor = ancestor.getParent()) != null) {
	    levels++;
	}

	return levels;
    }

}

