import java.lang.invoke.VarHandle;
import java.util.Comparator;

class ConcurrentSkipListMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt;
	implements ConcurrentNavigableMap&lt;K, V&gt;, Cloneable, Serializable {
    /**
     * {@inheritDoc}
     *
     * @return the previous value associated with the specified key,
     *         or {@code null} if there was no mapping for the key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key or value is null
     */
    public V replace(K key, V value) {
	if (key == null || value == null)
	    throw new NullPointerException();
	for (;;) {
	    Node&lt;K, V&gt; n;
	    V v;
	    if ((n = findNode(key)) == null)
		return null;
	    if ((v = n.val) != null && VAL.compareAndSet(n, v, value))
		return v;
	}
    }

    private static final VarHandle VAL;
    /**
     * The comparator used to maintain order in this map, or null if
     * using natural ordering.  (Non-private to simplify access in
     * nested classes.)
     * @serial
     */
    final Comparator&lt;? super K&gt; comparator;
    /** Lazily initialized topmost index of the skiplist. */
    private transient Index&lt;K, V&gt; head;
    private static final VarHandle RIGHT;
    private static final VarHandle NEXT;

    /**
     * Returns node holding key or null if no such, clearing out any
     * deleted nodes seen along the way.  Repeatedly traverses at
     * base-level looking for key starting at predecessor returned
     * from findPredecessor, processing base-level deletions as
     * encountered. Restarts occur, at traversal step encountering
     * node n, if n's key field is null, indicating it is a marker, so
     * its predecessor is deleted before continuing, which we help do
     * by re-finding a valid predecessor.  The traversal loops in
     * doPut, doRemove, and findNear all include the same checks.
     *
     * @param key the key
     * @return node holding key, or null if no such
     */
    private Node&lt;K, V&gt; findNode(Object key) {
	if (key == null)
	    throw new NullPointerException(); // don't postpone errors
	Comparator&lt;? super K&gt; cmp = comparator;
	Node&lt;K, V&gt; b;
	outer: while ((b = findPredecessor(key, cmp)) != null) {
	    for (;;) {
		Node&lt;K, V&gt; n;
		K k;
		V v;
		int c;
		if ((n = b.next) == null)
		    break outer; // empty
		else if ((k = n.key) == null)
		    break; // b is deleted
		else if ((v = n.val) == null)
		    unlinkNode(b, n); // n is deleted
		else if ((c = cpr(cmp, key, k)) &gt; 0)
		    b = n;
		else if (c == 0)
		    return n;
		else
		    break outer;
	    }
	}
	return null;
    }

    /**
     * Returns an index node with key strictly less than given key.
     * Also unlinks indexes to deleted nodes found along the way.
     * Callers rely on this side-effect of clearing indices to deleted
     * nodes.
     *
     * @param key if nonnull the key
     * @return a predecessor node of key, or null if uninitialized or null key
     */
    private Node&lt;K, V&gt; findPredecessor(Object key, Comparator&lt;? super K&gt; cmp) {
	Index&lt;K, V&gt; q;
	VarHandle.acquireFence();
	if ((q = head) == null || key == null)
	    return null;
	else {
	    for (Index&lt;K, V&gt; r, d;;) {
		while ((r = q.right) != null) {
		    Node&lt;K, V&gt; p;
		    K k;
		    if ((p = r.node) == null || (k = p.key) == null || p.val == null) // unlink index to deleted node
			RIGHT.compareAndSet(q, r, r.right);
		    else if (cpr(cmp, key, k) &gt; 0)
			q = r;
		    else
			break;
		}
		if ((d = q.down) != null)
		    q = d;
		else
		    return q.node;
	    }
	}
    }

    /**
     * Tries to unlink deleted node n from predecessor b (if both
     * exist), by first splicing in a marker if not already present.
     * Upon return, node n is sure to be unlinked from b, possibly
     * via the actions of some other thread.
     *
     * @param b if nonnull, predecessor
     * @param n if nonnull, node known to be deleted
     */
    static &lt;K, V&gt; void unlinkNode(Node&lt;K, V&gt; b, Node&lt;K, V&gt; n) {
	if (b != null && n != null) {
	    Node&lt;K, V&gt; f, p;
	    for (;;) {
		if ((f = n.next) != null && f.key == null) {
		    p = f.next; // already marked
		    break;
		} else if (NEXT.compareAndSet(n, f, new Node&lt;K, V&gt;(null, null, f))) {
		    p = f; // add marker
		    break;
		}
	    }
	    NEXT.compareAndSet(b, n, p);
	}
    }

    /**
     * Compares using comparator or natural ordering if null.
     * Called only by methods that have performed required type checks.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    static int cpr(Comparator c, Object x, Object y) {
	return (c != null) ? c.compare(x, y) : ((Comparable) x).compareTo(y);
    }

    class Node&lt;K, V&gt; {
	private static final VarHandle VAL;
	/**
	* The comparator used to maintain order in this map, or null if
	* using natural ordering.  (Non-private to simplify access in
	* nested classes.)
	* @serial
	*/
	final Comparator&lt;? super K&gt; comparator;
	/** Lazily initialized topmost index of the skiplist. */
	private transient Index&lt;K, V&gt; head;
	private static final VarHandle RIGHT;
	private static final VarHandle NEXT;

	Node(K key, V value, Node&lt;K, V&gt; next) {
	    this.key = key;
	    this.val = value;
	    this.next = next;
	}

    }

}

