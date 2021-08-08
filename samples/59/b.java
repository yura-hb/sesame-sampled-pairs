import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class ArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt;, Serializable {
    /**
     * Inserts the specified element at the tail of this queue if it is
     * possible to do so immediately without exceeding the queue's capacity,
     * returning {@code true} upon success and {@code false} if this queue
     * is full.  This method is generally preferable to method {@link #add},
     * which can fail to insert an element only by throwing an exception.
     *
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e) {
	Objects.requireNonNull(e);
	final ReentrantLock lock = this.lock;
	lock.lock();
	try {
	    if (count == items.length)
		return false;
	    else {
		enqueue(e);
		return true;
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
    /** items index for next put, offer, or add */
    int putIndex;
    /** Condition for waiting takes */
    private final Condition notEmpty;

    /**
     * Inserts element at current put position, advances, and signals.
     * Call only when holding lock.
     */
    private void enqueue(E e) {
	// assert lock.isHeldByCurrentThread();
	// assert lock.getHoldCount() == 1;
	// assert items[putIndex] == null;
	final Object[] items = this.items;
	items[putIndex] = e;
	if (++putIndex == items.length)
	    putIndex = 0;
	count++;
	notEmpty.signal();
    }

}

