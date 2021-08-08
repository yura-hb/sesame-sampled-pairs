abstract class ParentNode extends ChildNode {
    /**
     * Test whether this node has any children. Convenience shorthand
     * for (Node.getFirstChild()!=null)
     */
    public boolean hasChildNodes() {
	if (needsSyncChildren()) {
	    synchronizeChildren();
	}
	return firstChild != null;
    }

    /** First child. */
    protected ChildNode firstChild = null;

    /**
     * Override this method in subclass to hook in efficient
     * internal data structure.
     */
    protected void synchronizeChildren() {
	// By default just change the flag to avoid calling this method again
	needsSyncChildren(false);
    }

}

