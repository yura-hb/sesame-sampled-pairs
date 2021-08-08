import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

class LinkedBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt;, Serializable {
    /**
     * Returns an array containing all of the elements in this queue, in
     * proper sequence.
     *
     * &lt;p&gt;The returned array will be "safe" in that no references to it are
     * maintained by this queue.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * &lt;p&gt;This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this queue
     */
    public Object[] toArray() {
	fullyLock();
	try {
	    int size = count.get();
	    Object[] a = new Object[size];
	    int k = 0;
	    for (Node&lt;E&gt; p = head.next; p != null; p = p.next)
		a[k++] = p.item;
	    return a;
	} finally {
	    fullyUnlock();
	}
    }

    /** Current number of elements */
    private final AtomicInteger count = new AtomicInteger();
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

