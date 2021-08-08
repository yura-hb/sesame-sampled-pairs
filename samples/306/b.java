import javax.swing.event.TreeModelEvent;
import java.util.Hashtable;
import java.util.Stack;
import sun.swing.SwingUtilities2;

class FixedHeightLayoutCache extends AbstractLayoutCache {
    /**
     * &lt;p&gt;Invoked after nodes have been inserted into the tree.&lt;/p&gt;
     *
     * &lt;p&gt;e.path() returns the parent of the new nodes
     * &lt;p&gt;e.childIndices() returns the indices of the new nodes in
     * ascending order.
     */
    public void treeNodesInserted(TreeModelEvent e) {
	if (e != null) {
	    int changedIndexs[];
	    FHTreeStateNode changedParent = getNodeForPath(SwingUtilities2.getTreePath(e, getModel()), false, false);
	    int maxCounter;

	    changedIndexs = e.getChildIndices();
	    /* Only need to update the children if the node has been
	       expanded once. */
	    // PENDING(scott): make sure childIndexs is sorted!
	    if (changedParent != null && changedIndexs != null && (maxCounter = changedIndexs.length) &gt; 0) {
		boolean isVisible = (changedParent.isVisible() && changedParent.isExpanded());

		for (int counter = 0; counter &lt; maxCounter; counter++) {
		    changedParent.childInsertedAtModelIndex(changedIndexs[counter], isVisible);
		}
		if (isVisible && treeSelectionModel != null)
		    treeSelectionModel.resetRowSelection();
		if (changedParent.isVisible())
		    this.visibleNodesChanged();
	    }
	}
    }

    private Stack&lt;Stack&lt;TreePath&gt;&gt; tempStacks;
    /**
     * Maps from TreePath to a FHTreeStateNode.
     */
    private Hashtable&lt;TreePath, FHTreeStateNode&gt; treePathMapping;
    /** Number of rows currently visible. */
    private int rowCount;

    /**
     * Messages getTreeNodeForPage(path, onlyIfVisible, shouldCreate,
     * path.length) as long as path is non-null and the length is {@literal &gt;} 0.
     * Otherwise returns null.
     */
    private FHTreeStateNode getNodeForPath(TreePath path, boolean onlyIfVisible, boolean shouldCreate) {
	if (path != null) {
	    FHTreeStateNode node;

	    node = getMapping(path);
	    if (node != null) {
		if (onlyIfVisible && !node.isVisible())
		    return null;
		return node;
	    }
	    if (onlyIfVisible)
		return null;

	    // Check all the parent paths, until a match is found.
	    Stack&lt;TreePath&gt; paths;

	    if (tempStacks.size() == 0) {
		paths = new Stack&lt;TreePath&gt;();
	    } else {
		paths = tempStacks.pop();
	    }

	    try {
		paths.push(path);
		path = path.getParentPath();
		node = null;
		while (path != null) {
		    node = getMapping(path);
		    if (node != null) {
			// Found a match, create entries for all paths in
			// paths.
			while (node != null && paths.size() &gt; 0) {
			    path = paths.pop();
			    node = node.createChildFor(path.getLastPathComponent());
			}
			return node;
		    }
		    paths.push(path);
		    path = path.getParentPath();
		}
	    } finally {
		paths.removeAllElements();
		tempStacks.push(paths);
	    }
	    // If we get here it means they share a different root!
	    return null;
	}
	return null;
    }

    private void visibleNodesChanged() {
    }

    /**
     * Returns the node previously added for &lt;code&gt;path&lt;/code&gt;. This may
     * return null, if you to create a node use getNodeForPath.
     */
    private FHTreeStateNode getMapping(TreePath path) {
	return treePathMapping.get(path);
    }

    /**
     * Adjust the large row count of the AbstractTreeUI the receiver was
     * created with.
     */
    private void adjustRowCountBy(int changeAmount) {
	rowCount += changeAmount;
    }

    /**
     * Creates and returns an instance of FHTreeStateNode.
     */
    private FHTreeStateNode createNodeForValue(Object value, int childIndex) {
	return new FHTreeStateNode(value, childIndex, -1);
    }

    class FHTreeStateNode extends DefaultMutableTreeNode {
	private Stack&lt;Stack&lt;TreePath&gt;&gt; tempStacks;
	/**
	* Maps from TreePath to a FHTreeStateNode.
	*/
	private Hashtable&lt;TreePath, FHTreeStateNode&gt; treePathMapping;
	/** Number of rows currently visible. */
	private int rowCount;

	/**
	 * Returns true if this node is visible. This is determined by
	 * asking all the parents if they are expanded.
	 */
	public boolean isVisible() {
	    FHTreeStateNode parent = (FHTreeStateNode) getParent();

	    if (parent == null)
		return true;
	    return (parent.isExpanded() && parent.isVisible());
	}

	/**
	 * Returns true if this node is expanded.
	 */
	public boolean isExpanded() {
	    return isExpanded;
	}

	/**
	 * Messaged when a child has been inserted at index. For all the
	 * children that have a childIndex &ge; index their index is incremented
	 * by one.
	 */
	protected void childInsertedAtModelIndex(int index, boolean isExpandedAndVisible) {
	    FHTreeStateNode aChild;
	    int maxCounter = getChildCount();

	    for (int counter = 0; counter &lt; maxCounter; counter++) {
		aChild = (FHTreeStateNode) getChildAt(counter);
		if (aChild.childIndex &gt;= index) {
		    if (isExpandedAndVisible) {
			adjustRowBy(1, counter);
			adjustRowCountBy(1);
		    }
		    /* Since matched and children are always sorted by
		       index, no need to continue testing with the above. */
		    for (; counter &lt; maxCounter; counter++)
			((FHTreeStateNode) getChildAt(counter)).childIndex++;
		    childCount++;
		    return;
		}
	    }
	    // No children to adjust, but it was a child, so we still need
	    // to adjust nodes after this one.
	    if (isExpandedAndVisible) {
		adjustRowBy(1, maxCounter);
		adjustRowCountBy(1);
	    }
	    childCount++;
	}

	/**
	 * Creates a new node to represent &lt;code&gt;userObject&lt;/code&gt;.
	 * This does NOT check to ensure there isn't already a child node
	 * to manage &lt;code&gt;userObject&lt;/code&gt;.
	 */
	protected FHTreeStateNode createChildFor(Object userObject) {
	    int newChildIndex = treeModel.getIndexOfChild(getUserObject(), userObject);

	    if (newChildIndex &lt; 0)
		return null;

	    FHTreeStateNode aNode;
	    FHTreeStateNode child = createNodeForValue(userObject, newChildIndex);
	    int childRow;

	    if (isVisible()) {
		childRow = getRowToModelIndex(newChildIndex);
	    } else {
		childRow = -1;
	    }
	    child.row = childRow;
	    for (int counter = 0, maxCounter = getChildCount(); counter &lt; maxCounter; counter++) {
		aNode = (FHTreeStateNode) getChildAt(counter);
		if (aNode.childIndex &gt; newChildIndex) {
		    insert(child, counter);
		    return child;
		}
	    }
	    add(child);
	    return child;
	}

	/**
	 * Adjusts this node, its child, and its parent starting at
	 * an index of &lt;code&gt;index&lt;/code&gt; index is the index of the child
	 * to start adjusting from, which is not necessarily the model
	 * index.
	 */
	protected void adjustRowBy(int amount, int startIndex) {
	    // Could check isVisible, but probably isn't worth it.
	    if (isExpanded) {
		// children following startIndex.
		for (int counter = getChildCount() - 1; counter &gt;= startIndex; counter--)
		    ((FHTreeStateNode) getChildAt(counter)).adjustRowBy(amount);
	    }
	    // Parent
	    FHTreeStateNode parent = (FHTreeStateNode) getParent();

	    if (parent != null) {
		parent.adjustRowBy(amount, parent.getIndex(this) + 1);
	    }
	}

	/**
	 * Returns the row of the child with a model index of
	 * &lt;code&gt;index&lt;/code&gt;.
	 */
	public int getRowToModelIndex(int index) {
	    FHTreeStateNode child;
	    int lastRow = getRow() + 1;
	    int retValue = lastRow;

	    // This too could be a binary search!
	    for (int counter = 0, maxCounter = getChildCount(); counter &lt; maxCounter; counter++) {
		child = (FHTreeStateNode) getChildAt(counter);
		if (child.childIndex &gt;= index) {
		    if (child.childIndex == index)
			return child.row;
		    if (counter == 0)
			return getRow() + 1 + index;
		    return child.row - (child.childIndex - index);
		}
	    }
	    // YECK!
	    return getRow() + 1 + getTotalChildCount() - (childCount - index);
	}

	/**
	 * Adjusts the receiver, and all its children rows by
	 * &lt;code&gt;amount&lt;/code&gt;.
	 */
	protected void adjustRowBy(int amount) {
	    row += amount;
	    if (isExpanded) {
		for (int counter = getChildCount() - 1; counter &gt;= 0; counter--)
		    ((FHTreeStateNode) getChildAt(counter)).adjustRowBy(amount);
	    }
	}

	public FHTreeStateNode(Object userObject, int childIndex, int row) {
	    super(userObject);
	    this.childIndex = childIndex;
	    this.row = row;
	}

	/**
	 * Returns the row of the receiver.
	 */
	public int getRow() {
	    return row;
	}

	/**
	 * Returns the number of children in the receiver by descending all
	 * expanded nodes and messaging them with getTotalChildCount.
	 */
	public int getTotalChildCount() {
	    if (isExpanded()) {
		FHTreeStateNode parent = (FHTreeStateNode) getParent();
		int pIndex;

		if (parent != null && (pIndex = parent.getIndex(this)) + 1 &lt; parent.getChildCount()) {
		    // This node has a created sibling, to calc total
		    // child count directly from that!
		    FHTreeStateNode nextSibling = (FHTreeStateNode) parent.getChildAt(pIndex + 1);

		    return nextSibling.row - row - (nextSibling.childIndex - childIndex);
		} else {
		    int retCount = childCount;

		    for (int counter = getChildCount() - 1; counter &gt;= 0; counter--) {
			retCount += ((FHTreeStateNode) getChildAt(counter)).getTotalChildCount();
		    }
		    return retCount;
		}
	    }
	    return 0;
	}

    }

}

