import java.util.concurrent.locks.ReentrantLock;

class ArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt;, Serializable {
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
	final ReentrantLock lock = this.lock;
	lock.lock();
	try {
	    if (count &gt; 0) {
		final Object[] items = this.items;
		for (int i = takeIndex, end = putIndex, to = (i &lt; end) ? end : items.length;; i = 0, to = end) {
		    for (; i &lt; to; i++)
			if (o.equals(items[i]))
			    return true;
		    if (to == end)
			break;
		}
	    }
	    return false;
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

}

