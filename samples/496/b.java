class DefaultTreeModel implements Serializable, TreeModel {
    /**
     * Returns whether the specified node is a leaf node.
     * The way the test is performed depends on the
     * &lt;code&gt;askAllowsChildren&lt;/code&gt; setting.
     *
     * @param node the node to check
     * @return true if the node is a leaf node
     *
     * @see #asksAllowsChildren
     * @see TreeModel#isLeaf
     */
    public boolean isLeaf(Object node) {
	if (asksAllowsChildren)
	    return !((TreeNode) node).getAllowsChildren();
	return ((TreeNode) node).isLeaf();
    }

    /**
      * Determines how the &lt;code&gt;isLeaf&lt;/code&gt; method figures
      * out if a node is a leaf node. If true, a node is a leaf
      * node if it does not allow children. (If it allows
      * children, it is not a leaf node, even if no children
      * are present.) That lets you distinguish between &lt;i&gt;folder&lt;/i&gt;
      * nodes and &lt;i&gt;file&lt;/i&gt; nodes in a file system, for example.
      * &lt;p&gt;
      * If this value is false, then any node which has no
      * children is a leaf node, and any node may acquire
      * children.
      *
      * @see TreeNode#getAllowsChildren
      * @see TreeModel#isLeaf
      * @see #setAsksAllowsChildren
      */
    protected boolean asksAllowsChildren;

}

