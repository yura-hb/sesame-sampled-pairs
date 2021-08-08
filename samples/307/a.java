class BufferManager {
    /**
    * Returns an enumeration of all open buffers.
    * &lt;p&gt;
    * The &lt;code&gt;Enumeration&lt;/code&gt; answered is thread safe.
    *
    * @see OverflowingLRUCache
    * @return Enumeration of IBuffer
    */
    public Enumeration&lt;IBuffer&gt; getOpenBuffers() {
	Enumeration&lt;IBuffer&gt; result;
	synchronized (this.openBuffers) {
	    this.openBuffers.shrink();
	    result = this.openBuffers.elements();
	}
	// close buffers that were removed from the cache if space was needed
	this.openBuffers.closeBuffers();
	return result;
    }

    /**
     * LRU cache of buffers. The key and value for an entry
     * in the table is the identical buffer.
     */
    private BufferCache&lt;IOpenable&gt; openBuffers = new BufferCache&lt;&gt;(60);

}

