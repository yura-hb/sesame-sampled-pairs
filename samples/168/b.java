import java.util.*;

class DefaultMutableTreeNode implements Cloneable, MutableTreeNode, Serializable {
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

}

