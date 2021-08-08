import java.util.*;

class DefaultMutableTreeNode implements Cloneable, MutableTreeNode, Serializable {
    /**
     * Removes all of this node's children, setting their parents to null.
     * If this node has no children, this method does nothing.
     */
    public void removeAllChildren() {
	for (int i = getChildCount() - 1; i &gt;= 0; i--) {
	    remove(i);
	}
    }

    /** array of children, may be null if this node has no children */
    protected Vector&lt;TreeNode&gt; children;

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

