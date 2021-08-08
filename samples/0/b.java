import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

class PriorityBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt;, Serializable {
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
	    return Arrays.copyOf(queue, size);
	} finally {
	    lock.unlock();
	}
    }

    /**
     * Lock used for all public operations.
     */
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * Priority queue represented as a balanced binary heap: the two
     * children of queue[n] are queue[2*n+1] and queue[2*(n+1)].  The
     * priority queue is ordered by comparator, or by the elements'
     * natural ordering, if comparator is null: For each node n in the
     * heap and each descendant d of n, n &lt;= d.  The element with the
     * lowest value is in queue[0], assuming the queue is nonempty.
     */
    private transient Object[] queue;
    /**
     * The number of elements in the priority queue.
     */
    private transient int size;

}

