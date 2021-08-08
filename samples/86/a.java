import java.util.Hashtable;

class LRUCache&lt;K, V&gt; implements Cloneable {
    /**
     * Flushes all entries from the cache.
     */
    public void flush() {
	this.currentSpace = 0;
	LRUCacheEntry&lt;K, V&gt; entry = this.entryQueueTail; // Remember last entry
	this.entryTable = new Hashtable&lt;&gt;(); // Clear it out
	this.entryQueue = this.entryQueueTail = null;
	while (entry != null) { // send deletion notifications in LRU order
	    entry = entry.previous;
	}
    }

    /**
     * Amount of cache space used so far
     */
    protected int currentSpace;
    /**
     * End of queue (least recently used entry)
     */
    protected LRUCacheEntry&lt;K, V&gt; entryQueueTail;
    /**
     * Hash table for fast random access to cache entries
     */
    protected Hashtable&lt;K, LRUCacheEntry&lt;K, V&gt;&gt; entryTable;
    /**
     * Start of queue (most recently used entry)
     */
    protected LRUCacheEntry&lt;K, V&gt; entryQueue;

}

