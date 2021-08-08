import java.util.concurrent.locks.ReentrantLock;

class LinkedBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt;, Serializable {
    /**
     * Returns {@code true} if this queue contains the specified element.
     * More formally, returns {@code true} if and only if this queue contains
     * at least one element {@code e} such that {@code o.equals(e)}.
     *
     * @param o object to be checked for containment in this queue
     * @return {@code true} if this queue contains the specified element
     */
    public boolean contains(Object o) {
	if (o == null)
	    return false;
	fullyLock();
	try {
	    for (Node&lt;E&gt; p = head.next; p != null; p = p.next)
		if (o.equals(p.item))
		    return true;
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
     * Locks to prevent both puts and takes.
     */
    void fullyLock() {
	putLock.lock();
	takeLock.lock();
    }

    /**
     * Unlocks to allow both puts and takes.
     */
    void fullyUnlock() {
	takeLock.unlock();
	putLock.unlock();
    }

}

