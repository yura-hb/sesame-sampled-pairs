class NodeCachingLinkedList&lt;E&gt; extends AbstractLinkedList&lt;E&gt; implements Serializable {
    /**
     * Adds a node to the cache, if the cache isn't full.
     * The node's contents are cleared to so they can be garbage collected.
     *
     * @param node  the node to add to the cache
     */
    protected void addNodeToCache(final Node&lt;E&gt; node) {
	if (isCacheFull()) {
	    // don't cache the node.
	    return;
	}
	// clear the node's contents and add it to the cache.
	final Node&lt;E&gt; nextCachedNode = firstCachedNode;
	node.previous = null;
	node.next = nextCachedNode;
	node.setValue(null);
	firstCachedNode = node;
	cacheSize++;
    }

    /**
     * The first cached node, or &lt;code&gt;null&lt;/code&gt; if no nodes are cached.
     * Cached nodes are stored in a singly-linked list with
     * &lt;code&gt;next&lt;/code&gt; pointing to the next element.
     */
    private transient Node&lt;E&gt; firstCachedNode;
    /**
     * The size of the cache.
     */
    private transient int cacheSize;
    /**
     * The maximum size of the cache.
     */
    private int maximumCacheSize;

    /**
     * Checks whether the cache is full.
     *
     * @return true if the cache is full
     */
    protected boolean isCacheFull() {
	return cacheSize &gt;= maximumCacheSize;
    }

}

