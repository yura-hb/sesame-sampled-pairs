class MindMapLinkRegistry {
    /**
     * @param node
     * @return null, if not registered.
     */
    public String getState(MindMapNode node) {
	if (mTargetToId.containsKey(node))
	    return (String) mTargetToId.get(node);
	return null;
    }

    /** MindMapNode = Target -&gt; ID. */
    protected HashMap&lt;MindMapNode, String&gt; mTargetToId;

}

