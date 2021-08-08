import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class ArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt;, Serializable {
    /**
     * Inserts the specified element at the tail of this queue, waiting
     * up to the specified wait time for space to become available if
     * the queue is full.
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {

	Objects.requireNonNull(e);
	long nanos = unit.toNanos(timeout);
	final ReentrantLock lock = this.lock;
	lock.lockInterruptibly();
	try {
	    while (count == items.length) {
		if (nanos &lt;= 0L)
		    return false;
		nanos = notFull.awaitNanos(nanos);
	    }
	    enqueue(e);
	    return true;
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
    /** Condition for waiting puts */
    private final Condition notFull;
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

