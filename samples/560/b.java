import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

class ArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt;, Serializable {
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
	final ReentrantLock lock = this.lock;
	lock.lock();
	try {
	    final Object[] items = this.items;
	    final int end = takeIndex + count;
	    final Object[] a = Arrays.copyOfRange(items, takeIndex, end);
	    if (end != putIndex)
		System.arraycopy(items, 0, a, items.length - takeIndex, putIndex);
	    return a;
	} finally {
	    lock.unlock();
	}
    }

    /** Main lock guarding all access */
    final ReentrantLock lock;
    /** The queued items */
    final Object[] items;
    /** items index for next take, poll, peek or remove */
    int takeIndex;
    /** Number of elements in the queue */
    int count;
    /** items index for next put, offer, or add */
    int putIndex;

}

