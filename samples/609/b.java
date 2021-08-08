import java.util.Arrays;
import java.util.Objects;

class LinkedTransferQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements TransferQueue&lt;E&gt;, Serializable {
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
	Objects.requireNonNull(a);
	return (T[]) toArrayInternal(a);
    }

    /**
     * A node from which the first live (non-matched) node (if any)
     * can be reached in O(1) time.
     * Invariants:
     * - all live nodes are reachable from head via .next
     * - head != null
     * - (tmp = head).next != tmp || tmp != head
     * Non-invariants:
     * - head may or may not be live
     * - it is permitted for tail to lag behind head, that is, for tail
     *   to not be reachable from head!
     */
    transient volatile Node head;

    private Object[] toArrayInternal(Object[] a) {
	Object[] x = a;
	restartFromHead: for (;;) {
	    int size = 0;
	    for (Node p = head; p != null;) {
		Object item = p.item;
		if (p.isData) {
		    if (item != null) {
			if (x == null)
			    x = new Object[4];
			else if (size == x.length)
			    x = Arrays.copyOf(x, 2 * (size + 4));
			x[size++] = item;
		    }
		} else if (item == null)
		    break;
		if (p == (p = p.next))
		    continue restartFromHead;
	    }
	    if (x == null)
		return new Object[0];
	    else if (a != null && size &lt;= a.length) {
		if (a != x)
		    System.arraycopy(x, 0, a, 0, size);
		if (size &lt; a.length)
		    a[size] = null;
		return a;
	    }
	    return (size == x.length) ? x : Arrays.copyOf(x, size);
	}
    }

}

