import java.util.*;

class DefaultMutableTreeNode implements Cloneable, MutableTreeNode, Serializable {
    /**
     * Removes &lt;code&gt;newChild&lt;/code&gt; from its parent and makes it a child of
     * this node by adding it to the end of this node's child array.
     *
     * @see             #insert
     * @param   newChild        node to add as a child of this node
     * @exception       IllegalArgumentException    if &lt;code&gt;newChild&lt;/code&gt;
     *                                          is null
     * @exception       IllegalStateException   if this node does not allow
     *                                          children
     */
    public void add(MutableTreeNode newChild) {
	if (newChild != null && newChild.getParent() == this)
	    insert(newChild, getChildCount() - 1);
	else
	    insert(newChild, getChildCount());
    }

    /** array of children, may be null if this node has no children */
    protected Vector&lt;TreeNode&gt; children;
    /** true if the node is able to have children */
    protected boolean allowsChildren;

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
     * Removes &lt;code&gt;newChild&lt;/code&gt; from its present parent (if it has a
     * parent), sets the child's parent to this node, and then adds the child
     * to this node's child array at index &lt;code&gt;childIndex&lt;/code&gt;.
     * &lt;code&gt;newChild&lt;/code&gt; must not be null and must not be an ancestor of
     * this node.
     *
     * @param   newChild        the MutableTreeNode to insert under this node
     * @param   childIndex      the index in this node's child array
     *                          where this node is to be inserted
     * @exception       ArrayIndexOutOfBoundsException  if
     *                          &lt;code&gt;childIndex&lt;/code&gt; is out of bounds
     * @exception       IllegalArgumentException        if
     *                          &lt;code&gt;newChild&lt;/code&gt; is null or is an
     *                          ancestor of this node
     * @exception       IllegalStateException   if this node does not allow
     *                                          children
     * @see     #isNodeDescendant
     */
    public void insert(MutableTreeNode newChild, int childIndex) {
	if (!allowsChildren) {
	    throw new IllegalStateException("node does not allow children");
	} else if (newChild == null) {
	    throw new IllegalArgumentException("new child is null");
	} else if (isNodeAncestor(newChild)) {
	    throw new IllegalArgumentException("new child is an ancestor");
	}

	MutableTreeNode oldParent = (MutableTreeNode) newChild.getParent();

	if (oldParent != null) {
	    oldParent.remove(newChild);
	}
	newChild.setParent(this);
	if (children == null) {
	    children = new Vector&lt;&gt;();
	}
	children.insertElementAt(newChild, childIndex);
    }

    /**
     * Returns true if &lt;code&gt;anotherNode&lt;/code&gt; is an ancestor of this node
     * -- if it is this node, this node's parent, or an ancestor of this
     * node's parent.  (Note that a node is considered an ancestor of itself.)
     * If &lt;code&gt;anotherNode&lt;/code&gt; is null, this method returns false.  This
     * operation is at worst O(h) where h is the distance from the root to
     * this node.
     *
     * @see             #isNodeDescendant
     * @see             #getSharedAncestor
     * @param   anotherNode     node to test as an ancestor of this node
     * @return  true if this node is a descendant of &lt;code&gt;anotherNode&lt;/code&gt;
     */
    public boolean isNodeAncestor(TreeNode anotherNode) {
	if (anotherNode == null) {
	    return false;
	}

	TreeNode ancestor = this;

	do {
	    if (ancestor == anotherNode) {
		return true;
	    }
	} while ((ancestor = ancestor.getParent()) != null);

	return false;
    }

}

