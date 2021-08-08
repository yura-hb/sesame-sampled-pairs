import java.util.Hashtable;

class LRUCache&lt;K, V&gt; implements Cloneable {
    /**
     * Returns a new cache containing the same contents.
     *
     * @return New copy of object.
     */
    @Override
    public LRUCache&lt;K, V&gt; clone() {
	LRUCache&lt;K, V&gt; newCache = newInstance(this.spaceLimit);
	LRUCacheEntry&lt;K, V&gt; qEntry;

	/* Preserve order of entries by copying from oldest to newest */
	qEntry = this.entryQueueTail;
	while (qEntry != null) {
	    newCache.privateAdd(qEntry.key, qEntry.value, qEntry.space);
	    qEntry = qEntry.previous;
	}
	return newCache;
    }

    /**
     * Maximum space allowed in cache
     */
    protected int spaceLimit;
    /**
     * End of queue (least recently used entry)
     */
    protected LRUCacheEntry&lt;K, V&gt; entryQueueTail;
    /**
     * Counter for handing out sequential timestamps
     */
    protected int timestampCounter;
    /**
     * Amount of cache space used so far
     */
    protected int currentSpace;
    /**
     * Start of queue (most recently used entry)
     */
    protected LRUCacheEntry&lt;K, V&gt; entryQueue;
    /**
     * Hash table for fast random access to cache entries
     */
    protected Hashtable&lt;K, LRUCacheEntry&lt;K, V&gt;&gt; entryTable;

    /**
     * Returns a new LRUCache instance
     */
    protected LRUCache&lt;K, V&gt; newInstance(int size) {
	return new LRUCache&lt;&gt;(size);
    }

    /**
     * Adds an entry for the given key/value/space.
     */
    protected void privateAdd(K key, V value, int space) {
	LRUCacheEntry&lt;K, V&gt; entry;
	entry = new LRUCacheEntry&lt;&gt;(key, value, space);
	privateAddEntry(entry, false);
    }

    /**
     * Creates a new cache.
     * @param size Size of Cache
     */
    public LRUCache(int size) {
	this.timestampCounter = this.currentSpace = 0;
	this.entryQueue = this.entryQueueTail = null;
	this.entryTable = new Hashtable&lt;&gt;(size);
	this.spaceLimit = size;
    }

    /**
     * Adds the given entry from the receiver.
     * @param shuffle Indicates whether we are just shuffling the queue
     * (in which case, the entry table is not modified).
     */
    protected void privateAddEntry(LRUCacheEntry&lt;K, V&gt; entry, boolean shuffle) {
	if (!shuffle) {
	    this.entryTable.put(entry.key, entry);
	    this.currentSpace += entry.space;
	}

	entry.timestamp = this.timestampCounter++;
	entry.next = this.entryQueue;
	entry.previous = null;

	if (this.entryQueue == null) {
	    /* this is the first and last entry */
	    this.entryQueueTail = entry;
	} else {
	    this.entryQueue.previous = entry;
	}

	this.entryQueue = entry;
    }

    class LRUCacheEntry&lt;K, V&gt; {
	/**
	 * Maximum space allowed in cache
	 */
	protected int spaceLimit;
	/**
	 * End of queue (least recently used entry)
	 */
	protected LRUCacheEntry&lt;K, V&gt; entryQueueTail;
	/**
	 * Counter for handing out sequential timestamps
	 */
	protected int timestampCounter;
	/**
	 * Amount of cache space used so far
	 */
	protected int currentSpace;
	/**
	 * Start of queue (most recently used entry)
	 */
	protected LRUCacheEntry&lt;K, V&gt; entryQueue;
	/**
	 * Hash table for fast random access to cache entries
	 */
	protected Hashtable&lt;K, LRUCacheEntry&lt;K, V&gt;&gt; entryTable;

	/**
		 * Creates a new instance of the receiver with the provided values
		 * for key, value, and space.
		 */
	public LRUCacheEntry(K key, V value, int space) {
	    this.key = key;
	    this.value = value;
	    this.space = space;
	}

    }

}

