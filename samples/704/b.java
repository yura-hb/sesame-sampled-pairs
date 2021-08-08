import java.util.Hashtable;
import java.util.Stack;

class FixedHeightLayoutCache extends AbstractLayoutCache {
    /**
     * Returns an Enumerator that increments over the visible paths
     * starting at the passed in location. The ordering of the enumeration
     * is based on how the paths are displayed.
     */
    public Enumeration&lt;TreePath&gt; getVisiblePathsFrom(TreePath path) {
	if (path == null)
	    return null;

	FHTreeStateNode node = getNodeForPath(path, true, false);

	if (node != null) {
	    return new VisibleFHTreeStateNodeEnumeration(node);
	}
	TreePath parentPath = path.getParentPath();

	node = getNodeForPath(parentPath, true, false);
	if (node != null && node.isExpanded()) {
	    return new VisibleFHTreeStateNodeEnumeration(node,
		    treeModel.getIndexOfChild(parentPath.getLastPathComponent(), path.getLastPathComponent()));
	}
	return null;
    }

    private Stack&lt;Stack&lt;TreePath&gt;&gt; tempStacks;
    /**
     * Maps from TreePath to a FHTreeStateNode.
     */
    private Hashtable&lt;TreePath, FHTreeStateNode&gt; treePathMapping;

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

    /**
     * Returns the node previously added for &lt;code&gt;path&lt;/code&gt;. This may
     * return null, if you to create a node use getNodeForPath.
     */
    private FHTreeStateNode getMapping(TreePath path) {
	return treePathMapping.get(path);
    }

    /**
     * Creates and returns an instance of FHTreeStateNode.
     */
    private FHTreeStateNode createNodeForValue(Object value, int childIndex) {
	return new FHTreeStateNode(value, childIndex, -1);
    }

    class VisibleFHTreeStateNodeEnumeration implements Enumeration&lt;TreePath&gt; {
	private Stack&lt;Stack&lt;TreePath&gt;&gt; tempStacks;
	/**
	* Maps from TreePath to a FHTreeStateNode.
	*/
	private Hashtable&lt;TreePath, FHTreeStateNode&gt; treePathMapping;

	protected VisibleFHTreeStateNodeEnumeration(FHTreeStateNode node) {
	    this(node, -1);
	}

	protected VisibleFHTreeStateNodeEnumeration(FHTreeStateNode parent, int startIndex) {
	    this.parent = parent;
	    this.nextIndex = startIndex;
	    this.childCount = treeModel.getChildCount(this.parent.getUserObject());
	}

    }

    class FHTreeStateNode extends DefaultMutableTreeNode {
	private Stack&lt;Stack&lt;TreePath&gt;&gt; tempStacks;
	/**
	* Maps from TreePath to a FHTreeStateNode.
	*/
	private Hashtable&lt;TreePath, FHTreeStateNode&gt; treePathMapping;

	/**
	 * Returns true if this node is expanded.
	 */
	public boolean isExpanded() {
	    return isExpanded;
	}

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

