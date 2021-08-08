import java.lang.invoke.VarHandle;
import java.util.AbstractMap;
import java.util.Comparator;

class ConcurrentSkipListMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt;
	implements ConcurrentNavigableMap&lt;K, V&gt;, Cloneable, Serializable {
    /**
     * Returns a key-value mapping associated with the least key
     * greater than or equal to the given key, or {@code null} if
     * there is no such entry. The returned entry does &lt;em&gt;not&lt;/em&gt;
     * support the {@code Entry.setValue} method.
     *
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     */
    public Map.Entry&lt;K, V&gt; ceilingEntry(K key) {
	return findNearEntry(key, GT | EQ, comparator);
    }

    private static final int GT = 0;
    private static final int EQ = 1;
    /**
     * The comparator used to maintain order in this map, or null if
     * using natural ordering.  (Non-private to simplify access in
     * nested classes.)
     * @serial
     */
    final Comparator&lt;? super K&gt; comparator;
    private static final int LT = 2;
    /** Lazily initialized topmost index of the skiplist. */
    private transient Index&lt;K, V&gt; head;
    private static final VarHandle RIGHT;
    private static final VarHandle NEXT;

    /**
     * Variant of findNear returning SimpleImmutableEntry
     * @param key the key
     * @param rel the relation -- OR'ed combination of EQ, LT, GT
     * @return Entry fitting relation, or null if no such
     */
    final AbstractMap.SimpleImmutableEntry&lt;K, V&gt; findNearEntry(K key, int rel, Comparator&lt;? super K&gt; cmp) {
	for (;;) {
	    Node&lt;K, V&gt; n;
	    V v;
	    if ((n = findNear(key, rel, cmp)) == null)
		return null;
	    if ((v = n.val) != null)
		return new AbstractMap.SimpleImmutableEntry&lt;K, V&gt;(n.key, v);
	}
    }

    /**
     * Utility for ceiling, floor, lower, higher methods.
     * @param key the key
     * @param rel the relation -- OR'ed combination of EQ, LT, GT
     * @return nearest node fitting relation, or null if no such
     */
    final Node&lt;K, V&gt; findNear(K key, int rel, Comparator&lt;? super K&gt; cmp) {
	if (key == null)
	    throw new NullPointerException();
	Node&lt;K, V&gt; result;
	outer: for (Node&lt;K, V&gt; b;;) {
	    if ((b = findPredecessor(key, cmp)) == null) {
		result = null;
		break; // empty
	    }
	    for (;;) {
		Node&lt;K, V&gt; n;
		K k;
		int c;
		if ((n = b.next) == null) {
		    result = ((rel & LT) != 0 && b.key != null) ? b : null;
		    break outer;
		} else if ((k = n.key) == null)
		    break;
		else if (n.val == null)
		    unlinkNode(b, n);
		else if (((c = cpr(cmp, key, k)) == 0 && (rel & EQ) != 0) || (c &lt; 0 && (rel & LT) == 0)) {
		    result = n;
		    break outer;
		} else if (c &lt;= 0 && (rel & LT) != 0) {
		    result = (b.key != null) ? b : null;
		    break outer;
		} else
		    b = n;
	    }
	}
	return result;
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
	private static final int GT = 0;
	private static final int EQ = 1;
	/**
	* The comparator used to maintain order in this map, or null if
	* using natural ordering.  (Non-private to simplify access in
	* nested classes.)
	* @serial
	*/
	final Comparator&lt;? super K&gt; comparator;
	private static final int LT = 2;
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

