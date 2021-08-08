import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class ArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt;, Serializable {
    /**
     * Atomically removes all of the elements from this queue.
     * The queue will be empty after this call returns.
     */
    public void clear() {
	final ReentrantLock lock = this.lock;
	lock.lock();
	try {
	    int k;
	    if ((k = count) &gt; 0) {
		circularClear(items, takeIndex, putIndex);
		takeIndex = putIndex;
		count = 0;
		if (itrs != null)
		    itrs.queueIsEmpty();
		for (; k &gt; 0 && lock.hasWaiters(notFull); k--)
		    notFull.signal();
	    }
	} finally {
	    lock.unlock();
	}
    }

    /** Main lock guarding all access */
    final ReentrantLock lock;
    /** Number of elements in the queue */
    int count;
    /** The queued items */
    final Object[] items;
    /** items index for next take, poll, peek or remove */
    int takeIndex;
    /** items index for next put, offer, or add */
    int putIndex;
    /**
     * Shared state for currently active iterators, or null if there
     * are known not to be any.  Allows queue operations to update
     * iterator state.
     */
    transient Itrs itrs;
    /** Condition for waiting puts */
    private final Condition notFull;

    /**
     * Nulls out slots starting at array index i, upto index end.
     * Condition i == end means "full" - the entire array is cleared.
     */
    private static void circularClear(Object[] items, int i, int end) {
	// assert 0 &lt;= i && i &lt; items.length;
	// assert 0 &lt;= end && end &lt; items.length;
	for (int to = (i &lt; end) ? end : items.length;; i = 0, to = end) {
	    for (; i &lt; to; i++)
		items[i] = null;
	    if (to == end)
		break;
	}
    }

    class Itrs {
	/** Main lock guarding all access */
	final ReentrantLock lock;
	/** Number of elements in the queue */
	int count;
	/** The queued items */
	final Object[] items;
	/** items index for next take, poll, peek or remove */
	int takeIndex;
	/** items index for next put, offer, or add */
	int putIndex;
	/**
	* Shared state for currently active iterators, or null if there
	* are known not to be any.  Allows queue operations to update
	* iterator state.
	*/
	transient Itrs itrs;
	/** Condition for waiting puts */
	private final Condition notFull;

	/**
	 * Called whenever the queue becomes empty.
	 *
	 * Notifies all active iterators that the queue is empty,
	 * clears all weak refs, and unlinks the itrs datastructure.
	 */
	void queueIsEmpty() {
	    // assert lock.isHeldByCurrentThread();
	    for (Node p = head; p != null; p = p.next) {
		Itr it = p.get();
		if (it != null) {
		    p.clear();
		    it.shutdown();
		}
	    }
	    head = null;
	    itrs = null;
	}

    }

    class Itr implements Iterator&lt;E&gt; {
	/** Main lock guarding all access */
	final ReentrantLock lock;
	/** Number of elements in the queue */
	int count;
	/** The queued items */
	final Object[] items;
	/** items index for next take, poll, peek or remove */
	int takeIndex;
	/** items index for next put, offer, or add */
	int putIndex;
	/**
	* Shared state for currently active iterators, or null if there
	* are known not to be any.  Allows queue operations to update
	* iterator state.
	*/
	transient Itrs itrs;
	/** Condition for waiting puts */
	private final Condition notFull;

	/**
	 * Called to notify the iterator that the queue is empty, or that it
	 * has fallen hopelessly behind, so that it should abandon any
	 * further iteration, except possibly to return one more element
	 * from next(), as promised by returning true from hasNext().
	 */
	void shutdown() {
	    // assert lock.isHeldByCurrentThread();
	    cursor = NONE;
	    if (nextIndex &gt;= 0)
		nextIndex = REMOVED;
	    if (lastRet &gt;= 0) {
		lastRet = REMOVED;
		lastItem = null;
	    }
	    prevTakeIndex = DETACHED;
	    // Don't set nextItem to null because we must continue to be
	    // able to return it on next().
	    //
	    // Caller will unlink from itrs when convenient.
	}

    }

}

