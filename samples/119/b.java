class DefaultTreeModel implements Serializable, TreeModel {
    /**
     * Returns the index of child in parent.
     * If either the parent or child is &lt;code&gt;null&lt;/code&gt;, returns -1.
     * @param parent a note in the tree, obtained from this data source
     * @param child the node we are interested in
     * @return the index of the child in the parent, or -1
     *    if either the parent or the child is &lt;code&gt;null&lt;/code&gt;
     */
    public int getIndexOfChild(Object parent, Object child) {
	if (parent == null || child == null)
	    return -1;
	return ((TreeNode) parent).getIndex((TreeNode) child);
    }

}

