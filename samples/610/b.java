import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Objects;

class ConcurrentLinkedQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements Queue&lt;E&gt;, Serializable {
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
     * A node from which the first live (non-deleted) node (if any)
     * can be reached in O(1) time.
     * Invariants:
     * - all live nodes are reachable from head via succ()
     * - head != null
     * - (tmp = head).next != tmp || tmp != head
     * Non-invariants:
     * - head.item may or may not be null.
     * - it is permitted for tail to lag behind head, that is, for tail
     *   to not be reachable from head!
     */
    transient volatile Node&lt;E&gt; head;
    private static final VarHandle HEAD;
    static final VarHandle NEXT;

    private Object[] toArrayInternal(Object[] a) {
	Object[] x = a;
	restartFromHead: for (;;) {
	    int size = 0;
	    for (Node&lt;E&gt; p = first(); p != null;) {
		final E item;
		if ((item = p.item) != null) {
		    if (x == null)
			x = new Object[4];
		    else if (size == x.length)
			x = Arrays.copyOf(x, 2 * (size + 4));
		    x[size++] = item;
		}
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

    /**
     * Returns the first live (non-deleted) node on list, or null if none.
     * This is yet another variant of poll/peek; here returning the
     * first node, not element.  We could make peek() a wrapper around
     * first(), but that would cost an extra volatile read of item,
     * and the need to add a retry loop to deal with the possibility
     * of losing a race to a concurrent poll().
     */
    Node&lt;E&gt; first() {
	restartFromHead: for (;;) {
	    for (Node&lt;E&gt; h = head, p = h, q;; p = q) {
		boolean hasItem = (p.item != null);
		if (hasItem || (q = p.next) == null) {
		    updateHead(h, p);
		    return hasItem ? p : null;
		} else if (p == q)
		    continue restartFromHead;
	    }
	}
    }

    /**
     * Tries to CAS head to p. If successful, repoint old head to itself
     * as sentinel for succ(), below.
     */
    final void updateHead(Node&lt;E&gt; h, Node&lt;E&gt; p) {
	// assert h != null && p != null && (h == p || h.item == null);
	if (h != p && HEAD.compareAndSet(this, h, p))
	    NEXT.setRelease(h, h);
    }

}

