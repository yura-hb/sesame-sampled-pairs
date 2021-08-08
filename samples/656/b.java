class DefaultMutableTreeNode implements Cloneable, MutableTreeNode, Serializable {
    /**
     * Builds the parents of node up to and including the root node,
     * where the original node is the last element in the returned array.
     * The length of the returned array gives the node's depth in the
     * tree.
     *
     * @param aNode  the TreeNode to get the path for
     * @param depth  an int giving the number of steps already taken towards
     *        the root (on recursive calls), used to size the returned array
     * @return an array of TreeNodes giving the path from the root to the
     *         specified node
     */
    protected TreeNode[] getPathToRoot(TreeNode aNode, int depth) {
	TreeNode[] retNodes;

	/* Check for null, in case someone passed in a null node, or
	   they passed in an element that isn't rooted at root. */
	if (aNode == null) {
	    if (depth == 0)
		return null;
	    else
		retNodes = new TreeNode[depth];
	} else {
	    depth++;
	    retNodes = getPathToRoot(aNode.getParent(), depth);
	    retNodes[retNodes.length - depth] = aNode;
	}
	return retNodes;
    }

}

