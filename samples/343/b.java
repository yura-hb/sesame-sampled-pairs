import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

class MapMakerInternalMap&lt;K, V, E, S&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    abstract class HashIterator&lt;T&gt; implements Iterator&lt;T&gt; {
	/** Finds the next entry in the current chain. Returns {@code true} if an entry was found. */
	boolean nextInChain() {
	    if (nextEntry != null) {
		for (nextEntry = nextEntry.getNext(); nextEntry != null; nextEntry = nextEntry.getNext()) {
		    if (advanceTo(nextEntry)) {
			return true;
		    }
		}
	    }
	    return false;
	}

	@Nullable
	E nextEntry;
	@Nullable
	WriteThroughEntry nextExternal;
	@MonotonicNonNull
	Segment&lt;K, V, E, S&gt; currentSegment;

	/**
	 * Advances to the given entry. Returns {@code true} if the entry was valid, {@code false} if it
	 * should be skipped.
	 */
	boolean advanceTo(E entry) {
	    try {
		K key = entry.getKey();
		V value = getLiveValue(entry);
		if (value != null) {
		    nextExternal = new WriteThroughEntry(key, value);
		    return true;
		} else {
		    // Skip stale entry.
		    return false;
		}
	    } finally {
		currentSegment.postReadCleanup();
	    }
	}

    }

    /**
    * Number of cache access operations that can be buffered per segment before the cache's recency
    * ordering information is updated. This is used to avoid lock contention by recording a memento
    * of reads and delaying a lock acquisition until the threshold is crossed or a mutation occurs.
    *
    * &lt;p&gt;This must be a (2^n)-1 as it is used as a mask.
    */
    static final int DRAIN_THRESHOLD = 0x3F;

    /**
    * Gets the value from an entry. Returns {@code null} if the entry is invalid, partially-collected
    * or computing.
    */
    V getLiveValue(E entry) {
	if (entry.getKey() == null) {
	    return null;
	}
	V value = entry.getValue();
	if (value == null) {
	    return null;
	}
	return value;
    }

    interface InternalEntry&lt;K, V, E&gt; {
	/**
	* Number of cache access operations that can be buffered per segment before the cache's recency
	* ordering information is updated. This is used to avoid lock contention by recording a memento
	* of reads and delaying a lock acquisition until the threshold is crossed or a mutation occurs.
	*
	* &lt;p&gt;This must be a (2^n)-1 as it is used as a mask.
	*/
	static final int DRAIN_THRESHOLD = 0x3F;

	/** Gets the next entry in the chain. */
	E getNext();

	/** Gets the key for this entry. */
	K getKey();

	/** Gets the value for the entry. */
	V getValue();

    }

    class WriteThroughEntry extends AbstractMapEntry&lt;K, V&gt; {
	/**
	* Number of cache access operations that can be buffered per segment before the cache's recency
	* ordering information is updated. This is used to avoid lock contention by recording a memento
	* of reads and delaying a lock acquisition until the threshold is crossed or a mutation occurs.
	*
	* &lt;p&gt;This must be a (2^n)-1 as it is used as a mask.
	*/
	static final int DRAIN_THRESHOLD = 0x3F;

	WriteThroughEntry(K key, V value) {
	    this.key = key;
	    this.value = value;
	}

    }

    abstract class Segment&lt;K, V, E, S&gt; extends ReentrantLock {
	/**
	* Number of cache access operations that can be buffered per segment before the cache's recency
	* ordering information is updated. This is used to avoid lock contention by recording a memento
	* of reads and delaying a lock acquisition until the threshold is crossed or a mutation occurs.
	*
	* &lt;p&gt;This must be a (2^n)-1 as it is used as a mask.
	*/
	static final int DRAIN_THRESHOLD = 0x3F;

	/**
	* Performs routine cleanup following a read. Normally cleanup happens during writes, or from
	* the cleanupExecutor. If cleanup is not observed after a sufficient number of reads, try
	* cleaning up from the read thread.
	*/
	void postReadCleanup() {
	    if ((readCount.incrementAndGet() & DRAIN_THRESHOLD) == 0) {
		runCleanup();
	    }
	}

	void runCleanup() {
	    runLockedCleanup();
	}

	void runLockedCleanup() {
	    if (tryLock()) {
		try {
		    maybeDrainReferenceQueues();
		    readCount.set(0);
		} finally {
		    unlock();
		}
	    }
	}

	/** Drains the reference queues used by this segment, if any. */
	@GuardedBy("this")
	void maybeDrainReferenceQueues() {
	}

    }

}

