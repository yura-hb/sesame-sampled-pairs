import java.util.*;

class DefaultMutableTreeNode implements Cloneable, MutableTreeNode, Serializable {
    /**
     * Returns the next sibling of this node in the parent's children array.
     * Returns null if this node has no parent or is the parent's last child.
     * This method performs a linear search that is O(n) where n is the number
     * of children; to traverse the entire array, use the parent's child
     * enumeration instead.
     *
     * @see     #children
     * @return  the sibling of this node that immediately follows this node
     */
    public DefaultMutableTreeNode getNextSibling() {
	DefaultMutableTreeNode retval;

	DefaultMutableTreeNode myParent = (DefaultMutableTreeNode) getParent();

	if (myParent == null) {
	    retval = null;
	} else {
	    retval = (DefaultMutableTreeNode) myParent.getChildAfter(this); // linear search
	}

	if (retval != null && !isNodeSibling(retval)) {
	    throw new Error("child of parent is not a sibling");
	}

	return retval;
    }

    /** this node's parent, or null if this node has no parent */
    protected MutableTreeNode parent;
    /** array of children, may be null if this node has no children */
    protected Vector&lt;TreeNode&gt; children;

    /**
     * Returns this node's parent or null if this node has no parent.
     *
     * @return  this node's parent TreeNode, or null if this node has no parent
     */
    public TreeNode getParent() {
	return parent;
    }

    /**
     * Returns the child in this node's child array that immediately
     * follows &lt;code&gt;aChild&lt;/code&gt;, which must be a child of this node.  If
     * &lt;code&gt;aChild&lt;/code&gt; is the last child, returns null.  This method
     * performs a linear search of this node's children for
     * &lt;code&gt;aChild&lt;/code&gt; and is O(n) where n is the number of children; to
     * traverse the entire array of children, use an enumeration instead.
     *
     * @param           aChild the child node to look for next child after it
     * @see             #children
     * @exception       IllegalArgumentException if &lt;code&gt;aChild&lt;/code&gt; is
     *                                  null or is not a child of this node
     * @return  the child of this node that immediately follows
     *          &lt;code&gt;aChild&lt;/code&gt;
     */
    public TreeNode getChildAfter(TreeNode aChild) {
	if (aChild == null) {
	    throw new IllegalArgumentException("argument is null");
	}

	int index = getIndex(aChild); // linear search

	if (index == -1) {
	    throw new IllegalArgumentException("node is not a child");
	}

	if (index &lt; getChildCount() - 1) {
	    return getChildAt(index + 1);
	} else {
	    return null;
	}
    }

    /**
     * Returns true if &lt;code&gt;anotherNode&lt;/code&gt; is a sibling of (has the
     * same parent as) this node.  A node is its own sibling.  If
     * &lt;code&gt;anotherNode&lt;/code&gt; is null, returns false.
     *
     * @param   anotherNode     node to test as sibling of this node
     * @return  true if &lt;code&gt;anotherNode&lt;/code&gt; is a sibling of this node
     */
    public boolean isNodeSibling(TreeNode anotherNode) {
	boolean retval;

	if (anotherNode == null) {
	    retval = false;
	} else if (anotherNode == this) {
	    retval = true;
	} else {
	    TreeNode myParent = getParent();
	    retval = (myParent != null && myParent == anotherNode.getParent());

	    if (retval && !((DefaultMutableTreeNode) getParent()).isNodeChild(anotherNode)) {
		throw new Error("sibling has different parent");
	    }
	}

	return retval;
    }

    /**
     * Returns the index of the specified child in this node's child array.
     * If the specified node is not a child of this node, returns
     * &lt;code&gt;-1&lt;/code&gt;.  This method performs a linear search and is O(n)
     * where n is the number of children.
     *
     * @param   aChild  the TreeNode to search for among this node's children
     * @exception       IllegalArgumentException        if &lt;code&gt;aChild&lt;/code&gt;
     *                                                  is null
     * @return  an int giving the index of the node in this node's child
     *          array, or &lt;code&gt;-1&lt;/code&gt; if the specified node is a not
     *          a child of this node
     */
    public int getIndex(TreeNode aChild) {
	if (aChild == null) {
	    throw new IllegalArgumentException("argument is null");
	}

	if (!isNodeChild(aChild)) {
	    return -1;
	}
	return children.indexOf(aChild); // linear search
    }

    /**
     * Returns the number of children of this node.
     *
     * @return  an int giving the number of children of this node
     */
    public int getChildCount() {
	if (children == null) {
	    return 0;
	} else {
	    return children.size();
	}
    }

    /**
     * Returns the child at the specified index in this node's child array.
     *
     * @param   index   an index into this node's child array
     * @exception       ArrayIndexOutOfBoundsException  if &lt;code&gt;index&lt;/code&gt;
     *                                          is out of bounds
     * @return  the TreeNode in this node's child array at  the specified index
     */
    public TreeNode getChildAt(int index) {
	if (children == null) {
	    throw new ArrayIndexOutOfBoundsException("node has no children");
	}
	return children.elementAt(index);
    }

    /**
     * Returns true if &lt;code&gt;aNode&lt;/code&gt; is a child of this node.  If
     * &lt;code&gt;aNode&lt;/code&gt; is null, this method returns false.
     *
     * @param   aNode the node to determinate whether it is a child
     * @return  true if &lt;code&gt;aNode&lt;/code&gt; is a child of this node; false if
     *                  &lt;code&gt;aNode&lt;/code&gt; is null
     */
    public boolean isNodeChild(TreeNode aNode) {
	boolean retval;

	if (aNode == null) {
	    retval = false;
	} else {
	    if (getChildCount() == 0) {
		retval = false;
	    } else {
		retval = (aNode.getParent() == this);
	    }
	}

	return retval;
    }

}

