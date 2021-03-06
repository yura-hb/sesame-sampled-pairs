import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

class LinkedBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt;, Serializable {
    /**
     * Returns an array containing all of the elements in this queue, in
     * proper sequence; the runtime type of the returned array is that of
     * the specified array.  If the queue fits in the specified array, it
     * is returned therein.  Otherwise, a new array is allocated with the
     * runtime type of the specified array and the size of this queue.
     *
     * &lt;p&gt;If this queue fits in the specified array with room to spare
     * (i.e., the array has more elements than this queue), the element in
     * the array immediately following the end of the queue is set to
     * {@code null}.
     *
     * &lt;p&gt;Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * &lt;p&gt;Suppose {@code x} is a queue known to contain only strings.
     * The following code can be used to dump the queue into a newly
     * allocated array of {@code String}:
     *
     * &lt;pre&gt; {@code String[] y = x.toArray(new String[0]);}&lt;/pre&gt;
     *
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of the queue are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose
     * @return an array containing all of the elements in this queue
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this queue
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    public &lt;T&gt; T[] toArray(T[] a) {
	fullyLock();
	try {
	    int size = count.get();
	    if (a.length &lt; size)
		a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);

	    int k = 0;
	    for (Node&lt;E&gt; p = head.next; p != null; p = p.next)
		a[k++] = (T) p.item;
	    if (a.length &gt; k)
		a[k] = null;
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

