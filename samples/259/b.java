import java.util.concurrent.locks.ReentrantLock;

class PriorityBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt;, Serializable {
    /**
     * Returns {@code true} if this queue contains the specified element.
     * More formally, returns {@code true} if and only if this queue contains
     * at least one element {@code e} such that {@code o.equals(e)}.
     *
     * @param o object to be checked for containment in this queue
     * @return {@code true} if this queue contains the specified element
     */
    public boolean contains(Object o) {
	final ReentrantLock lock = this.lock;
	lock.lock();
	try {
	    return indexOf(o) != -1;
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

    private int indexOf(Object o) {
	if (o != null) {
	    final Object[] es = queue;
	    for (int i = 0, n = size; i &lt; n; i++)
		if (o.equals(es[i]))
		    return i;
	}
	return -1;
    }

}

