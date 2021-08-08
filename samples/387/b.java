import java.util.concurrent.locks.ReentrantLock;

class PriorityBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt;, Serializable {
    /**
     * Atomically removes all of the elements from this queue.
     * The queue will be empty after this call returns.
     */
    public void clear() {
	final ReentrantLock lock = this.lock;
	lock.lock();
	try {
	    final Object[] es = queue;
	    for (int i = 0, n = size; i &lt; n; i++)
		es[i] = null;
	    size = 0;
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

