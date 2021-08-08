import java.util.*;

class DefaultMutableTreeNode implements Cloneable, MutableTreeNode, Serializable {
    /**
     * Removes &lt;code&gt;aChild&lt;/code&gt; from this node's child array, giving it a
     * null parent.
     *
     * @param   aChild  a child of this node to remove
     * @exception       IllegalArgumentException        if &lt;code&gt;aChild&lt;/code&gt;
     *                                  is null or is not a child of this node
     */
    public void remove(MutableTreeNode aChild) {
	if (aChild == null) {
	    throw new IllegalArgumentException("argument is null");
	}

	if (!isNodeChild(aChild)) {
	    throw new IllegalArgumentException("argument is not a child");
	}
	remove(getIndex(aChild)); // linear search
    }

    /** array of children, may be null if this node has no children */
    protected Vector&lt;TreeNode&gt; children;

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
     * Removes the child at the specified index from this node's children
     * and sets that node's parent to null. The child node to remove
     * must be a &lt;code&gt;MutableTreeNode&lt;/code&gt;.
     *
     * @param   childIndex      the index in this node's child array
     *                          of the child to remove
     * @exception       ArrayIndexOutOfBoundsException  if
     *                          &lt;code&gt;childIndex&lt;/code&gt; is out of bounds
     */
    public void remove(int childIndex) {
	MutableTreeNode child = (MutableTreeNode) getChildAt(childIndex);
	children.removeElementAt(childIndex);
	child.setParent(null);
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

}

