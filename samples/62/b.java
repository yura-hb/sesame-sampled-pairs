import javax.swing.event.*;

class DefaultTreeModel implements Serializable, TreeModel {
    /**
      * Invoke this method if you've totally changed the children of
      * node and its children's children...  This will post a
      * treeStructureChanged event.
      *
      * @param node changed node
      */
    public void nodeStructureChanged(TreeNode node) {
	if (node != null) {
	    fireTreeStructureChanged(this, getPathToRoot(node), null, null);
	}
    }

    /** Listeners. */
    protected EventListenerList listenerList = new EventListenerList();
    /** Root of the tree. */
    protected TreeNode root;

    /**
     * Builds the parents of node up to and including the root node,
     * where the original node is the last element in the returned array.
     * The length of the returned array gives the node's depth in the
     * tree.
     *
     * @param aNode the TreeNode to get the path for
     * @return an array of TreeNodes giving the path from the root
     */
    public TreeNode[] getPathToRoot(TreeNode aNode) {
	return getPathToRoot(aNode, 0);
    }

    /**
     * Notifies all listeners that have registered interest for
     * notification on this event type.  The event instance
     * is lazily created using the parameters passed into
     * the fire method.
     *
     * @param source the source of the {@code TreeModelEvent};
     *               typically {@code this}
     * @param path the path to the parent of the structure that has changed;
     *             use {@code null} to identify the root has changed
     * @param childIndices the indices of the affected elements
     * @param children the affected elements
     */
    protected void fireTreeStructureChanged(Object source, Object[] path, int[] childIndices, Object[] children) {
	// Guaranteed to return a non-null array
	Object[] listeners = listenerList.getListenerList();
	TreeModelEvent e = null;
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length - 2; i &gt;= 0; i -= 2) {
	    if (listeners[i] == TreeModelListener.class) {
		// Lazily create the event:
		if (e == null)
		    e = new TreeModelEvent(source, path, childIndices, children);
		((TreeModelListener) listeners[i + 1]).treeStructureChanged(e);
	    }
	}
    }

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
	// This method recurses, traversing towards the root in order
	// size the array. On the way back, it fills in the nodes,
	// starting from the root and working back to the original node.

	/* Check for null, in case someone passed in a null node, or
	   they passed in an element that isn't rooted at root. */
	if (aNode == null) {
	    if (depth == 0)
		return null;
	    else
		retNodes = new TreeNode[depth];
	} else {
	    depth++;
	    if (aNode == root)
		retNodes = new TreeNode[depth];
	    else
		retNodes = getPathToRoot(aNode.getParent(), depth);
	    retNodes[retNodes.length - depth] = aNode;
	}
	return retNodes;
    }

}

