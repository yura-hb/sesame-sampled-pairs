import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

class DelayQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
     * Returns an array containing all of the elements in this queue.
     * The returned array elements are in no particular order.
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
	final ReentrantLock lock = this.lock;
	lock.lock();
	try {
	    return q.toArray();
	} finally {
	    lock.unlock();
	}
    }

    private final transient ReentrantLock lock = new ReentrantLock();
    private final PriorityQueue&lt;E&gt; q = new PriorityQueue&lt;E&gt;();

}

