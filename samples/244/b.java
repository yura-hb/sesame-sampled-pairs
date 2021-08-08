import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class LinkedBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt;, Serializable {
    /**
     * Removes a single instance of the specified element from this queue,
     * if it is present.  More formally, removes an element {@code e} such
     * that {@code o.equals(e)}, if this queue contains one or more such
     * elements.
     * Returns {@code true} if this queue contained the specified element
     * (or equivalently, if this queue changed as a result of the call).
     *
     * @param o element to be removed from this queue, if present
     * @return {@code true} if this queue changed as a result of the call
     */
    public boolean remove(Object o) {
	if (o == null)
	    return false;
	fullyLock();
	try {
	    for (Node&lt;E&gt; pred = head, p = pred.next; p != null; pred = p, p = p.next) {
		if (o.equals(p.item)) {
		    unlink(p, pred);
		    return true;
		}
	    }
	    return false;
	} finally {
	    fullyUnlock();
	}
    }

    /**
     * Head of linked list.
     * Invariant: head.item == null
     */
    transient Node&lt;E&gt; head;
    /** Lock held by put, offer, etc */
    private final ReentrantLock putLock = new ReentrantLock();
    /** Lock held by take, poll, etc */
    private final ReentrantLock takeLock = new ReentrantLock();
    /**
     * Tail of linked list.
     * Invariant: last.next == null
     */
    private transient Node&lt;E&gt; last;
    /** Current number of elements */
    private final AtomicInteger count = new AtomicInteger();
    /** The capacity bound, or Integer.MAX_VALUE if none */
    private final int capacity;
    /** Wait queue for waiting puts */
    private final Condition notFull = putLock.newCondition();

    /**
     * Locks to prevent both puts and takes.
     */
    void fullyLock() {
	putLock.lock();
	takeLock.lock();
    }

    /**
     * Unlinks interior Node p with predecessor pred.
     */
    void unlink(Node&lt;E&gt; p, Node&lt;E&gt; pred) {
	// assert putLock.isHeldByCurrentThread();
	// assert takeLock.isHeldByCurrentThread();
	// p.next is not changed, to allow iterators that are
	// traversing p to maintain their weak-consistency guarantee.
	p.item = null;
	pred.next = p.next;
	if (last == p)
	    last = pred;
	if (count.getAndDecrement() == capacity)
	    notFull.signal();
    }

    /**
     * Unlocks to allow both puts and takes.
     */
    void fullyUnlock() {
	takeLock.unlock();
	putLock.unlock();
    }

}

