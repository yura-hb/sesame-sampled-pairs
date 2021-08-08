import java.lang.invoke.VarHandle;
import java.util.Arrays;

class ConcurrentLinkedDeque&lt;E&gt; extends AbstractCollection&lt;E&gt; implements Deque&lt;E&gt;, Serializable {
    /**
     * Returns an array containing all of the elements in this deque,
     * in proper sequence (from first to last element); the runtime
     * type of the returned array is that of the specified array.  If
     * the deque fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of
     * the specified array and the size of this deque.
     *
     * &lt;p&gt;If this deque fits in the specified array with room to spare
     * (i.e., the array has more elements than this deque), the element in
     * the array immediately following the end of the deque is set to
     * {@code null}.
     *
     * &lt;p&gt;Like the {@link #toArray()} method, this method acts as
     * bridge between array-based and collection-based APIs.  Further,
     * this method allows precise control over the runtime type of the
     * output array, and may, under certain circumstances, be used to
     * save allocation costs.
     *
     * &lt;p&gt;Suppose {@code x} is a deque known to contain only strings.
     * The following code can be used to dump the deque into a newly
     * allocated array of {@code String}:
     *
     * &lt;pre&gt; {@code String[] y = x.toArray(new String[0]);}&lt;/pre&gt;
     *
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     *
     * @param a the array into which the elements of the deque are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose
     * @return an array containing all of the elements in this deque
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this deque
     * @throws NullPointerException if the specified array is null
     */
    @SuppressWarnings("unchecked")
    public &lt;T&gt; T[] toArray(T[] a) {
	if (a == null)
	    throw new NullPointerException();
	return (T[]) toArrayInternal(a);
    }

    /**
     * A node from which the first node on list (that is, the unique node p
     * with p.prev == null && p.next != p) can be reached in O(1) time.
     * Invariants:
     * - the first node is always O(1) reachable from head via prev links
     * - all live nodes are reachable from the first node via succ()
     * - head != null
     * - (tmp = head).next != tmp || tmp != head
     * - head is never gc-unlinked (but may be unlinked)
     * Non-invariants:
     * - head.item may or may not be null
     * - head may not be reachable from the first or last node, or from tail
     */
    private transient volatile Node&lt;E&gt; head;
    private static final VarHandle HEAD;

    private Object[] toArrayInternal(Object[] a) {
	Object[] x = a;
	restart: for (;;) {
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
		    continue restart;
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
     * Returns the first node, the unique node p for which:
     *     p.prev == null && p.next != p
     * The returned node may or may not be logically deleted.
     * Guarantees that head is set to the returned node.
     */
    Node&lt;E&gt; first() {
	restartFromHead: for (;;)
	    for (Node&lt;E&gt; h = head, p = h, q;;) {
		if ((q = p.prev) != null && (q = (p = q).prev) != null)
		    // Check for head updates every other hop.
		    // If p == q, we are sure to follow head instead.
		    p = (h != (h = head)) ? h : q;
		else if (p == h
			// It is possible that p is PREV_TERMINATOR,
			// but if so, the CAS is guaranteed to fail.
			|| HEAD.compareAndSet(this, h, p))
		    return p;
		else
		    continue restartFromHead;
	    }
    }

}

