import java.lang.invoke.VarHandle;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.concurrent.atomic.LongAdder;

class ConcurrentSkipListMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt;
	implements ConcurrentNavigableMap&lt;K, V&gt;, Cloneable, Serializable {
    /**
     * Attempts to compute a mapping for the specified key and its
     * current mapped value (or {@code null} if there is no current
     * mapping). The function is &lt;em&gt;NOT&lt;/em&gt; guaranteed to be applied
     * once atomically.
     *
     * @param key key with which the specified value is to be associated
     * @param remappingFunction the function to compute a value
     * @return the new value associated with the specified key, or null if none
     * @throws NullPointerException if the specified key is null
     *         or the remappingFunction is null
     * @since 1.8
     */
    public V compute(K key, BiFunction&lt;? super K, ? super V, ? extends V&gt; remappingFunction) {
	if (key == null || remappingFunction == null)
	    throw new NullPointerException();
	for (;;) {
	    Node&lt;K, V&gt; n;
	    V v;
	    V r;
	    if ((n = findNode(key)) == null) {
		if ((r = remappingFunction.apply(key, null)) == null)
		    break;
		if (doPut(key, r, true) == null)
		    return r;
	    } else if ((v = n.val) != null) {
		if ((r = remappingFunction.apply(key, v)) != null) {
		    if (VAL.compareAndSet(n, v, r))
			return r;
		} else if (doRemove(key, v) != null)
		    break;
	    }
	}
	return null;
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
    private static final VarHandle HEAD;
    private static final VarHandle RIGHT;
    private static final VarHandle NEXT;
    /** Lazily initialized element count */
    private transient LongAdder adder;
    private static final VarHandle ADDER;

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
     * Main insertion method.  Adds element if not present, or
     * replaces value if present and onlyIfAbsent is false.
     *
     * @param key the key
     * @param value the value that must be associated with key
     * @param onlyIfAbsent if should not insert if already present
     * @return the old value, or null if newly inserted
     */
    private V doPut(K key, V value, boolean onlyIfAbsent) {
	if (key == null)
	    throw new NullPointerException();
	Comparator&lt;? super K&gt; cmp = comparator;
	for (;;) {
	    Index&lt;K, V&gt; h;
	    Node&lt;K, V&gt; b;
	    VarHandle.acquireFence();
	    int levels = 0; // number of levels descended
	    if ((h = head) == null) { // try to initialize
		Node&lt;K, V&gt; base = new Node&lt;K, V&gt;(null, null, null);
		h = new Index&lt;K, V&gt;(base, null, null);
		b = (HEAD.compareAndSet(this, null, h)) ? base : null;
	    } else {
		for (Index&lt;K, V&gt; q = h, r, d;;) { // count while descending
		    while ((r = q.right) != null) {
			Node&lt;K, V&gt; p;
			K k;
			if ((p = r.node) == null || (k = p.key) == null || p.val == null)
			    RIGHT.compareAndSet(q, r, r.right);
			else if (cpr(cmp, key, k) &gt; 0)
			    q = r;
			else
			    break;
		    }
		    if ((d = q.down) != null) {
			++levels;
			q = d;
		    } else {
			b = q.node;
			break;
		    }
		}
	    }
	    if (b != null) {
		Node&lt;K, V&gt; z = null; // new node, if inserted
		for (;;) { // find insertion point
		    Node&lt;K, V&gt; n, p;
		    K k;
		    V v;
		    int c;
		    if ((n = b.next) == null) {
			if (b.key == null) // if empty, type check key now
			    cpr(cmp, key, key);
			c = -1;
		    } else if ((k = n.key) == null)
			break; // can't append; restart
		    else if ((v = n.val) == null) {
			unlinkNode(b, n);
			c = 1;
		    } else if ((c = cpr(cmp, key, k)) &gt; 0)
			b = n;
		    else if (c == 0 && (onlyIfAbsent || VAL.compareAndSet(n, v, value)))
			return v;

		    if (c &lt; 0 && NEXT.compareAndSet(b, n, p = new Node&lt;K, V&gt;(key, value, n))) {
			z = p;
			break;
		    }
		}

		if (z != null) {
		    int lr = ThreadLocalRandom.nextSecondarySeed();
		    if ((lr & 0x3) == 0) { // add indices with 1/4 prob
			int hr = ThreadLocalRandom.nextSecondarySeed();
			long rnd = ((long) hr &lt;&lt; 32) | ((long) lr & 0xffffffffL);
			int skips = levels; // levels to descend before add
			Index&lt;K, V&gt; x = null;
			for (;;) { // create at most 62 indices
			    x = new Index&lt;K, V&gt;(z, x, null);
			    if (rnd &gt;= 0L || --skips &lt; 0)
				break;
			    else
				rnd &lt;&lt;= 1;
			}
			if (addIndices(h, skips, x, cmp) && skips &lt; 0 && head == h) { // try to add new level
			    Index&lt;K, V&gt; hx = new Index&lt;K, V&gt;(z, x, null);
			    Index&lt;K, V&gt; nh = new Index&lt;K, V&gt;(h.node, h, hx);
			    HEAD.compareAndSet(this, h, nh);
			}
			if (z.val == null) // deleted while adding indices
			    findPredecessor(key, cmp); // clean
		    }
		    addCount(1L);
		    return null;
		}
	    }
	}
    }

    /**
     * Main deletion method. Locates node, nulls value, appends a
     * deletion marker, unlinks predecessor, removes associated index
     * nodes, and possibly reduces head index level.
     *
     * @param key the key
     * @param value if non-null, the value that must be
     * associated with key
     * @return the node, or null if not found
     */
    final V doRemove(Object key, Object value) {
	if (key == null)
	    throw new NullPointerException();
	Comparator&lt;? super K&gt; cmp = comparator;
	V result = null;
	Node&lt;K, V&gt; b;
	outer: while ((b = findPredecessor(key, cmp)) != null && result == null) {
	    for (;;) {
		Node&lt;K, V&gt; n;
		K k;
		V v;
		int c;
		if ((n = b.next) == null)
		    break outer;
		else if ((k = n.key) == null)
		    break;
		else if ((v = n.val) == null)
		    unlinkNode(b, n);
		else if ((c = cpr(cmp, key, k)) &gt; 0)
		    b = n;
		else if (c &lt; 0)
		    break outer;
		else if (value != null && !value.equals(v))
		    break outer;
		else if (VAL.compareAndSet(n, v, null)) {
		    result = v;
		    unlinkNode(b, n);
		    break; // loop to clean up
		}
	    }
	}
	if (result != null) {
	    tryReduceLevel();
	    addCount(-1L);
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

    /**
     * Add indices after an insertion. Descends iteratively to the
     * highest level of insertion, then recursively, to chain index
     * nodes to lower ones. Returns null on (staleness) failure,
     * disabling higher-level insertions. Recursion depths are
     * exponentially less probable.
     *
     * @param q starting index for current level
     * @param skips levels to skip before inserting
     * @param x index for this insertion
     * @param cmp comparator
     */
    static &lt;K, V&gt; boolean addIndices(Index&lt;K, V&gt; q, int skips, Index&lt;K, V&gt; x, Comparator&lt;? super K&gt; cmp) {
	Node&lt;K, V&gt; z;
	K key;
	if (x != null && (z = x.node) != null && (key = z.key) != null && q != null) { // hoist checks
	    boolean retrying = false;
	    for (;;) { // find splice point
		Index&lt;K, V&gt; r, d;
		int c;
		if ((r = q.right) != null) {
		    Node&lt;K, V&gt; p;
		    K k;
		    if ((p = r.node) == null || (k = p.key) == null || p.val == null) {
			RIGHT.compareAndSet(q, r, r.right);
			c = 0;
		    } else if ((c = cpr(cmp, key, k)) &gt; 0)
			q = r;
		    else if (c == 0)
			break; // stale
		} else
		    c = -1;

		if (c &lt; 0) {
		    if ((d = q.down) != null && skips &gt; 0) {
			--skips;
			q = d;
		    } else if (d != null && !retrying && !addIndices(d, 0, x.down, cmp))
			break;
		    else {
			x.right = r;
			if (RIGHT.compareAndSet(q, r, x))
			    return true;
			else
			    retrying = true; // re-find splice point
		    }
		}
	    }
	}
	return false;
    }

    /**
     * Adds to element count, initializing adder if necessary
     *
     * @param c count to add
     */
    private void addCount(long c) {
	LongAdder a;
	do {
	} while ((a = adder) == null && !ADDER.compareAndSet(this, null, a = new LongAdder()));
	a.add(c);
    }

    /**
     * Possibly reduce head level if it has no nodes.  This method can
     * (rarely) make mistakes, in which case levels can disappear even
     * though they are about to contain index nodes. This impacts
     * performance, not correctness.  To minimize mistakes as well as
     * to reduce hysteresis, the level is reduced by one only if the
     * topmost three levels look empty. Also, if the removed level
     * looks non-empty after CAS, we try to change it back quick
     * before anyone notices our mistake! (This trick works pretty
     * well because this method will practically never make mistakes
     * unless current thread stalls immediately before first CAS, in
     * which case it is very unlikely to stall again immediately
     * afterwards, so will recover.)
     *
     * We put up with all this rather than just let levels grow
     * because otherwise, even a small map that has undergone a large
     * number of insertions and removals will have a lot of levels,
     * slowing down access more than would an occasional unwanted
     * reduction.
     */
    private void tryReduceLevel() {
	Index&lt;K, V&gt; h, d, e;
	if ((h = head) != null && h.right == null && (d = h.down) != null && d.right == null && (e = d.down) != null
		&& e.right == null && HEAD.compareAndSet(this, h, d) && h.right != null) // recheck
	    HEAD.compareAndSet(this, d, h); // try to backout
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
	private static final VarHandle HEAD;
	private static final VarHandle RIGHT;
	private static final VarHandle NEXT;
	/** Lazily initialized element count */
	private transient LongAdder adder;
	private static final VarHandle ADDER;

	Node(K key, V value, Node&lt;K, V&gt; next) {
	    this.key = key;
	    this.val = value;
	    this.next = next;
	}

    }

    class Index&lt;K, V&gt; {
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
	private static final VarHandle HEAD;
	private static final VarHandle RIGHT;
	private static final VarHandle NEXT;
	/** Lazily initialized element count */
	private transient LongAdder adder;
	private static final VarHandle ADDER;

	Index(Node&lt;K, V&gt; node, Index&lt;K, V&gt; down, Index&lt;K, V&gt; right) {
	    this.node = node;
	    this.down = down;
	    this.right = right;
	}

    }

}

