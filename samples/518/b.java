import java.util.concurrent.locks.Condition;

class ArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt;, Serializable {
    /**
     * Deletes item at array index removeIndex.
     * Utility for remove(Object) and iterator.remove.
     * Call only when holding lock.
     */
    void removeAt(final int removeIndex) {
	// assert lock.isHeldByCurrentThread();
	// assert lock.getHoldCount() == 1;
	// assert items[removeIndex] != null;
	// assert removeIndex &gt;= 0 && removeIndex &lt; items.length;
	final Object[] items = this.items;
	if (removeIndex == takeIndex) {
	    // removing front item; just advance
	    items[takeIndex] = null;
	    if (++takeIndex == items.length)
		takeIndex = 0;
	    count--;
	    if (itrs != null)
		itrs.elementDequeued();
	} else {
	    // an "interior" remove

	    // slide over all others up through putIndex.
	    for (int i = removeIndex, putIndex = this.putIndex;;) {
		int pred = i;
		if (++i == items.length)
		    i = 0;
		if (i == putIndex) {
		    items[pred] = null;
		    this.putIndex = pred;
		    break;
		}
		items[pred] = items[i];
	    }
	    count--;
	    if (itrs != null)
		itrs.removedAt(removeIndex);
	}
	notFull.signal();
    }

    /** The queued items */
    final Object[] items;
    /** items index for next take, poll, peek or remove */
    int takeIndex;
    /** Number of elements in the queue */
    int count;
    /**
     * Shared state for currently active iterators, or null if there
     * are known not to be any.  Allows queue operations to update
     * iterator state.
     */
    transient Itrs itrs;
    /** items index for next put, offer, or add */
    int putIndex;
    /** Condition for waiting puts */
    private final Condition notFull;

    /**
     * Decrements i, mod modulus.
     * Precondition and postcondition: 0 &lt;= i &lt; modulus.
     */
    static final int dec(int i, int modulus) {
	if (--i &lt; 0)
	    i = modulus - 1;
	return i;
    }

    class Itrs {
	/** The queued items */
	final Object[] items;
	/** items index for next take, poll, peek or remove */
	int takeIndex;
	/** Number of elements in the queue */
	int count;
	/**
	* Shared state for currently active iterators, or null if there
	* are known not to be any.  Allows queue operations to update
	* iterator state.
	*/
	transient Itrs itrs;
	/** items index for next put, offer, or add */
	int putIndex;
	/** Condition for waiting puts */
	private final Condition notFull;

	/**
	 * Called whenever an element has been dequeued (at takeIndex).
	 */
	void elementDequeued() {
	    // assert lock.isHeldByCurrentThread();
	    if (count == 0)
		queueIsEmpty();
	    else if (takeIndex == 0)
		takeIndexWrapped();
	}

	/**
	 * Called whenever an interior remove (not at takeIndex) occurred.
	 *
	 * Notifies all iterators, and expunges any that are now stale.
	 */
	void removedAt(int removedIndex) {
	    for (Node o = null, p = head; p != null;) {
		final Itr it = p.get();
		final Node next = p.next;
		if (it == null || it.removedAt(removedIndex)) {
		    // unlink p
		    // assert it == null || it.isDetached();
		    p.clear();
		    p.next = null;
		    if (o == null)
			head = next;
		    else
			o.next = next;
		} else {
		    o = p;
		}
		p = next;
	    }
	    if (head == null) // no more iterators to track
		itrs = null;
	}

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

	/**
	 * Called whenever takeIndex wraps around to 0.
	 *
	 * Notifies all iterators, and expunges any that are now stale.
	 */
	void takeIndexWrapped() {
	    // assert lock.isHeldByCurrentThread();
	    cycles++;
	    for (Node o = null, p = head; p != null;) {
		final Itr it = p.get();
		final Node next = p.next;
		if (it == null || it.takeIndexWrapped()) {
		    // unlink p
		    // assert it == null || it.isDetached();
		    p.clear();
		    p.next = null;
		    if (o == null)
			head = next;
		    else
			o.next = next;
		} else {
		    o = p;
		}
		p = next;
	    }
	    if (head == null) // no more iterators to track
		itrs = null;
	}

    }

    class Itr implements Iterator&lt;E&gt; {
	/** The queued items */
	final Object[] items;
	/** items index for next take, poll, peek or remove */
	int takeIndex;
	/** Number of elements in the queue */
	int count;
	/**
	* Shared state for currently active iterators, or null if there
	* are known not to be any.  Allows queue operations to update
	* iterator state.
	*/
	transient Itrs itrs;
	/** items index for next put, offer, or add */
	int putIndex;
	/** Condition for waiting puts */
	private final Condition notFull;

	/**
	 * Called whenever an interior remove (not at takeIndex) occurred.
	 *
	 * @return true if this iterator should be unlinked from itrs
	 */
	boolean removedAt(int removedIndex) {
	    // assert lock.isHeldByCurrentThread();
	    if (isDetached())
		return true;

	    final int takeIndex = ArrayBlockingQueue.this.takeIndex;
	    final int prevTakeIndex = this.prevTakeIndex;
	    final int len = items.length;
	    // distance from prevTakeIndex to removedIndex
	    final int removedDistance = len * (itrs.cycles - this.prevCycles + ((removedIndex &lt; takeIndex) ? 1 : 0))
		    + (removedIndex - prevTakeIndex);
	    // assert itrs.cycles - this.prevCycles &gt;= 0;
	    // assert itrs.cycles - this.prevCycles &lt;= 1;
	    // assert removedDistance &gt; 0;
	    // assert removedIndex != takeIndex;
	    int cursor = this.cursor;
	    if (cursor &gt;= 0) {
		int x = distance(cursor, prevTakeIndex, len);
		if (x == removedDistance) {
		    if (cursor == putIndex)
			this.cursor = cursor = NONE;
		} else if (x &gt; removedDistance) {
		    // assert cursor != prevTakeIndex;
		    this.cursor = cursor = dec(cursor, len);
		}
	    }
	    int lastRet = this.lastRet;
	    if (lastRet &gt;= 0) {
		int x = distance(lastRet, prevTakeIndex, len);
		if (x == removedDistance)
		    this.lastRet = lastRet = REMOVED;
		else if (x &gt; removedDistance)
		    this.lastRet = lastRet = dec(lastRet, len);
	    }
	    int nextIndex = this.nextIndex;
	    if (nextIndex &gt;= 0) {
		int x = distance(nextIndex, prevTakeIndex, len);
		if (x == removedDistance)
		    this.nextIndex = nextIndex = REMOVED;
		else if (x &gt; removedDistance)
		    this.nextIndex = nextIndex = dec(nextIndex, len);
	    }
	    if (cursor &lt; 0 && nextIndex &lt; 0 && lastRet &lt; 0) {
		this.prevTakeIndex = DETACHED;
		return true;
	    }
	    return false;
	}

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

	/**
	 * Called whenever takeIndex wraps around to zero.
	 *
	 * @return true if this iterator should be unlinked from itrs
	 */
	boolean takeIndexWrapped() {
	    // assert lock.isHeldByCurrentThread();
	    if (isDetached())
		return true;
	    if (itrs.cycles - prevCycles &gt; 1) {
		// All the elements that existed at the time of the last
		// operation are gone, so abandon further iteration.
		shutdown();
		return true;
	    }
	    return false;
	}

	boolean isDetached() {
	    // assert lock.isHeldByCurrentThread();
	    return prevTakeIndex &lt; 0;
	}

	private int distance(int index, int prevTakeIndex, int length) {
	    int distance = index - prevTakeIndex;
	    if (distance &lt; 0)
		distance += length;
	    return distance;
	}

    }

}

