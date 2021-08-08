import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

class ArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt;, Serializable {
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
	final ReentrantLock lock = this.lock;
	lock.lock();
	try {
	    final Object[] items = this.items;
	    final int count = this.count;
	    final int firstLeg = Math.min(items.length - takeIndex, count);
	    if (a.length &lt; count) {
		a = (T[]) Arrays.copyOfRange(items, takeIndex, takeIndex + count, a.getClass());
	    } else {
		System.arraycopy(items, takeIndex, a, 0, firstLeg);
		if (a.length &gt; count)
		    a[count] = null;
	    }
	    if (firstLeg &lt; count)
		System.arraycopy(items, 0, a, firstLeg, putIndex);
	    return a;
	} finally {
	    lock.unlock();
	}
    }

    /** Main lock guarding all access */
    final ReentrantLock lock;
    /** The queued items */
    final Object[] items;
    /** Number of elements in the queue */
    int count;
    /** items index for next take, poll, peek or remove */
    int takeIndex;
    /** items index for next put, offer, or add */
    int putIndex;

}

