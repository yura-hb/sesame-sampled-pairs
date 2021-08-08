import java.lang.invoke.VarHandle;
import java.util.Comparator;
import java.util.concurrent.atomic.LongAdder;

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
    public V putIfAbsent(K key, V value) {
	if (value == null)
	    throw new NullPointerException();
	return doPut(key, value, true);
    }

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
    private static final VarHandle VAL;
    private static final VarHandle NEXT;
    /** Lazily initialized element count */
    private transient LongAdder adder;
    private static final VarHandle ADDER;

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
     * Compares using comparator or natural ordering if null.
     * Called only by methods that have performed required type checks.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    static int cpr(Comparator c, Object x, Object y) {
	return (c != null) ? c.compare(x, y) : ((Comparable) x).compareTo(y);
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

    class Node&lt;K, V&gt; {
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
	private static final VarHandle VAL;
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
	private static final VarHandle VAL;
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

