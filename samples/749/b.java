import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

class DelayQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
     * Returns an array containing all of the elements in this queue; the
     * runtime type of the returned array is that of the specified array.
     * The returned array elements are in no particular order.
     * If the queue fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this queue.
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
     * &lt;p&gt;The following code can be used to dump a delay queue into a newly
     * allocated array of {@code Delayed}:
     *
     * &lt;pre&gt; {@code Delayed[] a = q.toArray(new Delayed[0]);}&lt;/pre&gt;
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
    public &lt;T&gt; T[] toArray(T[] a) {
	final ReentrantLock lock = this.lock;
	lock.lock();
	try {
	    return q.toArray(a);
	} finally {
	    lock.unlock();
	}
    }

    private final transient ReentrantLock lock = new ReentrantLock();
    private final PriorityQueue&lt;E&gt; q = new PriorityQueue&lt;E&gt;();

}

