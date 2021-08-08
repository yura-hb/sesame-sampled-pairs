import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;
import jdk.internal.misc.Unsafe;

class ConcurrentHashMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    /**
     * Copies all of the mappings from the specified map to this one.
     * These mappings replace any mappings that this map had for any of the
     * keys currently in the specified map.
     *
     * @param m mappings to be stored in this map
     */
    public void putAll(Map&lt;? extends K, ? extends V&gt; m) {
	tryPresize(m.size());
	for (Map.Entry&lt;? extends K, ? extends V&gt; e : m.entrySet())
	    putVal(e.getKey(), e.getValue(), false);
    }

    /**
     * The largest possible table capacity.  This value must be
     * exactly 1&lt;&lt;30 to stay within Java array allocation and indexing
     * bounds for power of two table sizes, and is further required
     * because the top two bits of 32bit hash fields are used for
     * control purposes.
     */
    private static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
    /**
     * Table initialization and resizing control.  When negative, the
     * table is being initialized or resized: -1 for initialization,
     * else -(1 + the number of active resizing threads).  Otherwise,
     * when table is null, holds the initial table size to use upon
     * creation, or 0 for default. After initialization, holds the
     * next element count value upon which to resize the table.
     */
    private transient volatile int sizeCtl;
    /**
     * The array of bins. Lazily initialized upon first insertion.
     * Size is always a power of two. Accessed directly by iterators.
     */
    transient volatile Node&lt;K, V&gt;[] table;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final long SIZECTL;
    /**
     * The bit shift for recording size stamp in sizeCtl.
     */
    private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
    static final int MOVED = -1;
    /**
     * The bin count threshold for using a tree rather than list for a
     * bin.  Bins are converted to trees when adding an element to a
     * bin with at least this many nodes. The value must be greater
     * than 2, and should be at least 8 to mesh with assumptions in
     * tree removal about conversion back to plain bins upon
     * shrinkage.
     */
    static final int TREEIFY_THRESHOLD = 8;
    /**
     * The number of bits used for generation stamp in sizeCtl.
     * Must be at least 6 for 32bit arrays.
     */
    private static final int RESIZE_STAMP_BITS = 16;
    /** Number of CPUS, to place bounds on some sizings */
    static final int NCPU = Runtime.getRuntime().availableProcessors();
    /**
     * Minimum number of rebinnings per transfer step. Ranges are
     * subdivided to allow multiple resizer threads.  This value
     * serves as a lower bound to avoid resizers encountering
     * excessive memory contention.  The value should be at least
     * DEFAULT_CAPACITY.
     */
    private static final int MIN_TRANSFER_STRIDE = 16;
    /**
     * The next table to use; non-null only while resizing.
     */
    private transient volatile Node&lt;K, V&gt;[] nextTable;
    /**
     * The next table index (plus one) to split while resizing.
     */
    private transient volatile int transferIndex;
    private static final long TRANSFERINDEX;
    /**
     * The bin count threshold for untreeifying a (split) bin during a
     * resize operation. Should be less than TREEIFY_THRESHOLD, and at
     * most 6 to mesh with shrinkage detection under removal.
     */
    static final int UNTREEIFY_THRESHOLD = 6;
    static final int HASH_BITS = 0x7fffffff;
    /**
     * The default initial table capacity.  Must be a power of 2
     * (i.e., at least 1) and at most MAXIMUM_CAPACITY.
     */
    private static final int DEFAULT_CAPACITY = 16;
    private static final int ASHIFT;
    private static final int ABASE;
    /**
     * The maximum number of threads that can help resize.
     * Must fit in 32 - RESIZE_STAMP_BITS bits.
     */
    private static final int MAX_RESIZERS = (1 &lt;&lt; (32 - RESIZE_STAMP_BITS)) - 1;
    /**
     * The smallest table capacity for which bins may be treeified.
     * (Otherwise the table is resized if too many nodes in a bin.)
     * The value should be at least 4 * TREEIFY_THRESHOLD to avoid
     * conflicts between resizing and treeification thresholds.
     */
    static final int MIN_TREEIFY_CAPACITY = 64;
    /**
     * Table of counter cells. When non-null, size is a power of 2.
     */
    private transient volatile CounterCell[] counterCells;
    private static final long BASECOUNT;
    /**
     * Base counter value, used mainly when there is no contention,
     * but also as a fallback during table initialization
     * races. Updated via CAS.
     */
    private transient volatile long baseCount;
    private static final long CELLVALUE;
    static final int TREEBIN = -2;
    /**
     * Spinlock (locked via CAS) used when resizing and/or creating CounterCells.
     */
    private transient volatile int cellsBusy;
    private static final long CELLSBUSY;

    /**
     * Tries to presize table to accommodate the given number of elements.
     *
     * @param size number of elements (doesn't need to be perfectly accurate)
     */
    private final void tryPresize(int size) {
	int c = (size &gt;= (MAXIMUM_CAPACITY &gt;&gt;&gt; 1)) ? MAXIMUM_CAPACITY : tableSizeFor(size + (size &gt;&gt;&gt; 1) + 1);
	int sc;
	while ((sc = sizeCtl) &gt;= 0) {
	    Node&lt;K, V&gt;[] tab = table;
	    int n;
	    if (tab == null || (n = tab.length) == 0) {
		n = (sc &gt; c) ? sc : c;
		if (U.compareAndSetInt(this, SIZECTL, sc, -1)) {
		    try {
			if (table == tab) {
			    @SuppressWarnings("unchecked")
			    Node&lt;K, V&gt;[] nt = (Node&lt;K, V&gt;[]) new Node&lt;?, ?&gt;[n];
			    table = nt;
			    sc = n - (n &gt;&gt;&gt; 2);
			}
		    } finally {
			sizeCtl = sc;
		    }
		}
	    } else if (c &lt;= sc || n &gt;= MAXIMUM_CAPACITY)
		break;
	    else if (tab == table) {
		int rs = resizeStamp(n);
		if (U.compareAndSetInt(this, SIZECTL, sc, (rs &lt;&lt; RESIZE_STAMP_SHIFT) + 2))
		    transfer(tab, null);
	    }
	}
    }

    /** Implementation for put and putIfAbsent */
    final V putVal(K key, V value, boolean onlyIfAbsent) {
	if (key == null || value == null)
	    throw new NullPointerException();
	int hash = spread(key.hashCode());
	int binCount = 0;
	for (Node&lt;K, V&gt;[] tab = table;;) {
	    Node&lt;K, V&gt; f;
	    int n, i, fh;
	    K fk;
	    V fv;
	    if (tab == null || (n = tab.length) == 0)
		tab = initTable();
	    else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
		if (casTabAt(tab, i, null, new Node&lt;K, V&gt;(hash, key, value)))
		    break; // no lock when adding to empty bin
	    } else if ((fh = f.hash) == MOVED)
		tab = helpTransfer(tab, f);
	    else if (onlyIfAbsent // check first node without acquiring lock
		    && fh == hash && ((fk = f.key) == key || (fk != null && key.equals(fk))) && (fv = f.val) != null)
		return fv;
	    else {
		V oldVal = null;
		synchronized (f) {
		    if (tabAt(tab, i) == f) {
			if (fh &gt;= 0) {
			    binCount = 1;
			    for (Node&lt;K, V&gt; e = f;; ++binCount) {
				K ek;
				if (e.hash == hash && ((ek = e.key) == key || (ek != null && key.equals(ek)))) {
				    oldVal = e.val;
				    if (!onlyIfAbsent)
					e.val = value;
				    break;
				}
				Node&lt;K, V&gt; pred = e;
				if ((e = e.next) == null) {
				    pred.next = new Node&lt;K, V&gt;(hash, key, value);
				    break;
				}
			    }
			} else if (f instanceof TreeBin) {
			    Node&lt;K, V&gt; p;
			    binCount = 2;
			    if ((p = ((TreeBin&lt;K, V&gt;) f).putTreeVal(hash, key, value)) != null) {
				oldVal = p.val;
				if (!onlyIfAbsent)
				    p.val = value;
			    }
			} else if (f instanceof ReservationNode)
			    throw new IllegalStateException("Recursive update");
		    }
		}
		if (binCount != 0) {
		    if (binCount &gt;= TREEIFY_THRESHOLD)
			treeifyBin(tab, i);
		    if (oldVal != null)
			return oldVal;
		    break;
		}
	    }
	}
	addCount(1L, binCount);
	return null;
    }

    /**
     * Returns a power of two table size for the given desired capacity.
     * See Hackers Delight, sec 3.2
     */
    private static final int tableSizeFor(int c) {
	int n = -1 &gt;&gt;&gt; Integer.numberOfLeadingZeros(c - 1);
	return (n &lt; 0) ? 1 : (n &gt;= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * Returns the stamp bits for resizing a table of size n.
     * Must be negative when shifted left by RESIZE_STAMP_SHIFT.
     */
    static final int resizeStamp(int n) {
	return Integer.numberOfLeadingZeros(n) | (1 &lt;&lt; (RESIZE_STAMP_BITS - 1));
    }

    /**
     * Moves and/or copies the nodes in each bin to new table. See
     * above for explanation.
     */
    private final void transfer(Node&lt;K, V&gt;[] tab, Node&lt;K, V&gt;[] nextTab) {
	int n = tab.length, stride;
	if ((stride = (NCPU &gt; 1) ? (n &gt;&gt;&gt; 3) / NCPU : n) &lt; MIN_TRANSFER_STRIDE)
	    stride = MIN_TRANSFER_STRIDE; // subdivide range
	if (nextTab == null) { // initiating
	    try {
		@SuppressWarnings("unchecked")
		Node&lt;K, V&gt;[] nt = (Node&lt;K, V&gt;[]) new Node&lt;?, ?&gt;[n &lt;&lt; 1];
		nextTab = nt;
	    } catch (Throwable ex) { // try to cope with OOME
		sizeCtl = Integer.MAX_VALUE;
		return;
	    }
	    nextTable = nextTab;
	    transferIndex = n;
	}
	int nextn = nextTab.length;
	ForwardingNode&lt;K, V&gt; fwd = new ForwardingNode&lt;K, V&gt;(nextTab);
	boolean advance = true;
	boolean finishing = false; // to ensure sweep before committing nextTab
	for (int i = 0, bound = 0;;) {
	    Node&lt;K, V&gt; f;
	    int fh;
	    while (advance) {
		int nextIndex, nextBound;
		if (--i &gt;= bound || finishing)
		    advance = false;
		else if ((nextIndex = transferIndex) &lt;= 0) {
		    i = -1;
		    advance = false;
		} else if (U.compareAndSetInt(this, TRANSFERINDEX, nextIndex,
			nextBound = (nextIndex &gt; stride ? nextIndex - stride : 0))) {
		    bound = nextBound;
		    i = nextIndex - 1;
		    advance = false;
		}
	    }
	    if (i &lt; 0 || i &gt;= n || i + n &gt;= nextn) {
		int sc;
		if (finishing) {
		    nextTable = null;
		    table = nextTab;
		    sizeCtl = (n &lt;&lt; 1) - (n &gt;&gt;&gt; 1);
		    return;
		}
		if (U.compareAndSetInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
		    if ((sc - 2) != resizeStamp(n) &lt;&lt; RESIZE_STAMP_SHIFT)
			return;
		    finishing = advance = true;
		    i = n; // recheck before commit
		}
	    } else if ((f = tabAt(tab, i)) == null)
		advance = casTabAt(tab, i, null, fwd);
	    else if ((fh = f.hash) == MOVED)
		advance = true; // already processed
	    else {
		synchronized (f) {
		    if (tabAt(tab, i) == f) {
			Node&lt;K, V&gt; ln, hn;
			if (fh &gt;= 0) {
			    int runBit = fh & n;
			    Node&lt;K, V&gt; lastRun = f;
			    for (Node&lt;K, V&gt; p = f.next; p != null; p = p.next) {
				int b = p.hash & n;
				if (b != runBit) {
				    runBit = b;
				    lastRun = p;
				}
			    }
			    if (runBit == 0) {
				ln = lastRun;
				hn = null;
			    } else {
				hn = lastRun;
				ln = null;
			    }
			    for (Node&lt;K, V&gt; p = f; p != lastRun; p = p.next) {
				int ph = p.hash;
				K pk = p.key;
				V pv = p.val;
				if ((ph & n) == 0)
				    ln = new Node&lt;K, V&gt;(ph, pk, pv, ln);
				else
				    hn = new Node&lt;K, V&gt;(ph, pk, pv, hn);
			    }
			    setTabAt(nextTab, i, ln);
			    setTabAt(nextTab, i + n, hn);
			    setTabAt(tab, i, fwd);
			    advance = true;
			} else if (f instanceof TreeBin) {
			    TreeBin&lt;K, V&gt; t = (TreeBin&lt;K, V&gt;) f;
			    TreeNode&lt;K, V&gt; lo = null, loTail = null;
			    TreeNode&lt;K, V&gt; hi = null, hiTail = null;
			    int lc = 0, hc = 0;
			    for (Node&lt;K, V&gt; e = t.first; e != null; e = e.next) {
				int h = e.hash;
				TreeNode&lt;K, V&gt; p = new TreeNode&lt;K, V&gt;(h, e.key, e.val, null, null);
				if ((h & n) == 0) {
				    if ((p.prev = loTail) == null)
					lo = p;
				    else
					loTail.next = p;
				    loTail = p;
				    ++lc;
				} else {
				    if ((p.prev = hiTail) == null)
					hi = p;
				    else
					hiTail.next = p;
				    hiTail = p;
				    ++hc;
				}
			    }
			    ln = (lc &lt;= UNTREEIFY_THRESHOLD) ? untreeify(lo) : (hc != 0) ? new TreeBin&lt;K, V&gt;(lo) : t;
			    hn = (hc &lt;= UNTREEIFY_THRESHOLD) ? untreeify(hi) : (lc != 0) ? new TreeBin&lt;K, V&gt;(hi) : t;
			    setTabAt(nextTab, i, ln);
			    setTabAt(nextTab, i + n, hn);
			    setTabAt(tab, i, fwd);
			    advance = true;
			}
		    }
		}
	    }
	}
    }

    /**
     * Spreads (XORs) higher bits of hash to lower and also forces top
     * bit to 0. Because the table uses power-of-two masking, sets of
     * hashes that vary only in bits above the current mask will
     * always collide. (Among known examples are sets of Float keys
     * holding consecutive whole numbers in small tables.)  So we
     * apply a transform that spreads the impact of higher bits
     * downward. There is a tradeoff between speed, utility, and
     * quality of bit-spreading. Because many common sets of hashes
     * are already reasonably distributed (so don't benefit from
     * spreading), and because we use trees to handle large sets of
     * collisions in bins, we just XOR some shifted bits in the
     * cheapest possible way to reduce systematic lossage, as well as
     * to incorporate impact of the highest bits that would otherwise
     * never be used in index calculations because of table bounds.
     */
    static final int spread(int h) {
	return (h ^ (h &gt;&gt;&gt; 16)) & HASH_BITS;
    }

    /**
     * Initializes table, using the size recorded in sizeCtl.
     */
    private final Node&lt;K, V&gt;[] initTable() {
	Node&lt;K, V&gt;[] tab;
	int sc;
	while ((tab = table) == null || tab.length == 0) {
	    if ((sc = sizeCtl) &lt; 0)
		Thread.yield(); // lost initialization race; just spin
	    else if (U.compareAndSetInt(this, SIZECTL, sc, -1)) {
		try {
		    if ((tab = table) == null || tab.length == 0) {
			int n = (sc &gt; 0) ? sc : DEFAULT_CAPACITY;
			@SuppressWarnings("unchecked")
			Node&lt;K, V&gt;[] nt = (Node&lt;K, V&gt;[]) new Node&lt;?, ?&gt;[n];
			table = tab = nt;
			sc = n - (n &gt;&gt;&gt; 2);
		    }
		} finally {
		    sizeCtl = sc;
		}
		break;
	    }
	}
	return tab;
    }

    @SuppressWarnings("unchecked")
    static final &lt;K, V&gt; Node&lt;K, V&gt; tabAt(Node&lt;K, V&gt;[] tab, int i) {
	return (Node&lt;K, V&gt;) U.getObjectAcquire(tab, ((long) i &lt;&lt; ASHIFT) + ABASE);
    }

    static final &lt;K, V&gt; boolean casTabAt(Node&lt;K, V&gt;[] tab, int i, Node&lt;K, V&gt; c, Node&lt;K, V&gt; v) {
	return U.compareAndSetObject(tab, ((long) i &lt;&lt; ASHIFT) + ABASE, c, v);
    }

    /**
     * Helps transfer if a resize is in progress.
     */
    final Node&lt;K, V&gt;[] helpTransfer(Node&lt;K, V&gt;[] tab, Node&lt;K, V&gt; f) {
	Node&lt;K, V&gt;[] nextTab;
	int sc;
	if (tab != null && (f instanceof ForwardingNode) && (nextTab = ((ForwardingNode&lt;K, V&gt;) f).nextTable) != null) {
	    int rs = resizeStamp(tab.length);
	    while (nextTab == nextTable && table == tab && (sc = sizeCtl) &lt; 0) {
		if ((sc &gt;&gt;&gt; RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 || sc == rs + MAX_RESIZERS || transferIndex &lt;= 0)
		    break;
		if (U.compareAndSetInt(this, SIZECTL, sc, sc + 1)) {
		    transfer(tab, nextTab);
		    break;
		}
	    }
	    return nextTab;
	}
	return table;
    }

    /**
     * Replaces all linked nodes in bin at given index unless table is
     * too small, in which case resizes instead.
     */
    private final void treeifyBin(Node&lt;K, V&gt;[] tab, int index) {
	Node&lt;K, V&gt; b;
	int n;
	if (tab != null) {
	    if ((n = tab.length) &lt; MIN_TREEIFY_CAPACITY)
		tryPresize(n &lt;&lt; 1);
	    else if ((b = tabAt(tab, index)) != null && b.hash &gt;= 0) {
		synchronized (b) {
		    if (tabAt(tab, index) == b) {
			TreeNode&lt;K, V&gt; hd = null, tl = null;
			for (Node&lt;K, V&gt; e = b; e != null; e = e.next) {
			    TreeNode&lt;K, V&gt; p = new TreeNode&lt;K, V&gt;(e.hash, e.key, e.val, null, null);
			    if ((p.prev = tl) == null)
				hd = p;
			    else
				tl.next = p;
			    tl = p;
			}
			setTabAt(tab, index, new TreeBin&lt;K, V&gt;(hd));
		    }
		}
	    }
	}
    }

    /**
     * Adds to count, and if table is too small and not already
     * resizing, initiates transfer. If already resizing, helps
     * perform transfer if work is available.  Rechecks occupancy
     * after a transfer to see if another resize is already needed
     * because resizings are lagging additions.
     *
     * @param x the count to add
     * @param check if &lt;0, don't check resize, if &lt;= 1 only check if uncontended
     */
    private final void addCount(long x, int check) {
	CounterCell[] cs;
	long b, s;
	if ((cs = counterCells) != null || !U.compareAndSetLong(this, BASECOUNT, b = baseCount, s = b + x)) {
	    CounterCell c;
	    long v;
	    int m;
	    boolean uncontended = true;
	    if (cs == null || (m = cs.length - 1) &lt; 0 || (c = cs[ThreadLocalRandom.getProbe() & m]) == null
		    || !(uncontended = U.compareAndSetLong(c, CELLVALUE, v = c.value, v + x))) {
		fullAddCount(x, uncontended);
		return;
	    }
	    if (check &lt;= 1)
		return;
	    s = sumCount();
	}
	if (check &gt;= 0) {
	    Node&lt;K, V&gt;[] tab, nt;
	    int n, sc;
	    while (s &gt;= (long) (sc = sizeCtl) && (tab = table) != null && (n = tab.length) &lt; MAXIMUM_CAPACITY) {
		int rs = resizeStamp(n);
		if (sc &lt; 0) {
		    if ((sc &gt;&gt;&gt; RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 || sc == rs + MAX_RESIZERS
			    || (nt = nextTable) == null || transferIndex &lt;= 0)
			break;
		    if (U.compareAndSetInt(this, SIZECTL, sc, sc + 1))
			transfer(tab, nt);
		} else if (U.compareAndSetInt(this, SIZECTL, sc, (rs &lt;&lt; RESIZE_STAMP_SHIFT) + 2))
		    transfer(tab, null);
		s = sumCount();
	    }
	}
    }

    static final &lt;K, V&gt; void setTabAt(Node&lt;K, V&gt;[] tab, int i, Node&lt;K, V&gt; v) {
	U.putObjectRelease(tab, ((long) i &lt;&lt; ASHIFT) + ABASE, v);
    }

    /**
     * Returns a list of non-TreeNodes replacing those in given list.
     */
    static &lt;K, V&gt; Node&lt;K, V&gt; untreeify(Node&lt;K, V&gt; b) {
	Node&lt;K, V&gt; hd = null, tl = null;
	for (Node&lt;K, V&gt; q = b; q != null; q = q.next) {
	    Node&lt;K, V&gt; p = new Node&lt;K, V&gt;(q.hash, q.key, q.val);
	    if (tl == null)
		hd = p;
	    else
		tl.next = p;
	    tl = p;
	}
	return hd;
    }

    /**
     * Returns x's Class if it is of the form "class C implements
     * Comparable&lt;C&gt;", else null.
     */
    static Class&lt;?&gt; comparableClassFor(Object x) {
	if (x instanceof Comparable) {
	    Class&lt;?&gt; c;
	    Type[] ts, as;
	    ParameterizedType p;
	    if ((c = x.getClass()) == String.class) // bypass checks
		return c;
	    if ((ts = c.getGenericInterfaces()) != null) {
		for (Type t : ts) {
		    if ((t instanceof ParameterizedType)
			    && ((p = (ParameterizedType) t).getRawType() == Comparable.class)
			    && (as = p.getActualTypeArguments()) != null && as.length == 1 && as[0] == c) // type arg is c
			return c;
		}
	    }
	}
	return null;
    }

    /**
     * Returns k.compareTo(x) if x matches kc (k's screened comparable
     * class), else 0.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" }) // for cast to Comparable
    static int compareComparables(Class&lt;?&gt; kc, Object k, Object x) {
	return (x == null || x.getClass() != kc ? 0 : ((Comparable) k).compareTo(x));
    }

    private final void fullAddCount(long x, boolean wasUncontended) {
	int h;
	if ((h = ThreadLocalRandom.getProbe()) == 0) {
	    ThreadLocalRandom.localInit(); // force initialization
	    h = ThreadLocalRandom.getProbe();
	    wasUncontended = true;
	}
	boolean collide = false; // True if last slot nonempty
	for (;;) {
	    CounterCell[] cs;
	    CounterCell c;
	    int n;
	    long v;
	    if ((cs = counterCells) != null && (n = cs.length) &gt; 0) {
		if ((c = cs[(n - 1) & h]) == null) {
		    if (cellsBusy == 0) { // Try to attach new Cell
			CounterCell r = new CounterCell(x); // Optimistic create
			if (cellsBusy == 0 && U.compareAndSetInt(this, CELLSBUSY, 0, 1)) {
			    boolean created = false;
			    try { // Recheck under lock
				CounterCell[] rs;
				int m, j;
				if ((rs = counterCells) != null && (m = rs.length) &gt; 0 && rs[j = (m - 1) & h] == null) {
				    rs[j] = r;
				    created = true;
				}
			    } finally {
				cellsBusy = 0;
			    }
			    if (created)
				break;
			    continue; // Slot is now non-empty
			}
		    }
		    collide = false;
		} else if (!wasUncontended) // CAS already known to fail
		    wasUncontended = true; // Continue after rehash
		else if (U.compareAndSetLong(c, CELLVALUE, v = c.value, v + x))
		    break;
		else if (counterCells != cs || n &gt;= NCPU)
		    collide = false; // At max size or stale
		else if (!collide)
		    collide = true;
		else if (cellsBusy == 0 && U.compareAndSetInt(this, CELLSBUSY, 0, 1)) {
		    try {
			if (counterCells == cs) // Expand table unless stale
			    counterCells = Arrays.copyOf(cs, n &lt;&lt; 1);
		    } finally {
			cellsBusy = 0;
		    }
		    collide = false;
		    continue; // Retry with expanded table
		}
		h = ThreadLocalRandom.advanceProbe(h);
	    } else if (cellsBusy == 0 && counterCells == cs && U.compareAndSetInt(this, CELLSBUSY, 0, 1)) {
		boolean init = false;
		try { // Initialize table
		    if (counterCells == cs) {
			CounterCell[] rs = new CounterCell[2];
			rs[h & 1] = new CounterCell(x);
			counterCells = rs;
			init = true;
		    }
		} finally {
		    cellsBusy = 0;
		}
		if (init)
		    break;
	    } else if (U.compareAndSetLong(this, BASECOUNT, v = baseCount, v + x))
		break; // Fall back on using base
	}
    }

    final long sumCount() {
	CounterCell[] cs = counterCells;
	long sum = baseCount;
	if (cs != null) {
	    for (CounterCell c : cs)
		if (c != null)
		    sum += c.value;
	}
	return sum;
    }

    class Node&lt;K, V&gt; implements Entry&lt;K, V&gt; {
	/**
	* The largest possible table capacity.  This value must be
	* exactly 1&lt;&lt;30 to stay within Java array allocation and indexing
	* bounds for power of two table sizes, and is further required
	* because the top two bits of 32bit hash fields are used for
	* control purposes.
	*/
	private static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
	/**
	* Table initialization and resizing control.  When negative, the
	* table is being initialized or resized: -1 for initialization,
	* else -(1 + the number of active resizing threads).  Otherwise,
	* when table is null, holds the initial table size to use upon
	* creation, or 0 for default. After initialization, holds the
	* next element count value upon which to resize the table.
	*/
	private transient volatile int sizeCtl;
	/**
	* The array of bins. Lazily initialized upon first insertion.
	* Size is always a power of two. Accessed directly by iterators.
	*/
	transient volatile Node&lt;K, V&gt;[] table;
	private static final Unsafe U = Unsafe.getUnsafe();
	private static final long SIZECTL;
	/**
	* The bit shift for recording size stamp in sizeCtl.
	*/
	private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
	static final int MOVED = -1;
	/**
	* The bin count threshold for using a tree rather than list for a
	* bin.  Bins are converted to trees when adding an element to a
	* bin with at least this many nodes. The value must be greater
	* than 2, and should be at least 8 to mesh with assumptions in
	* tree removal about conversion back to plain bins upon
	* shrinkage.
	*/
	static final int TREEIFY_THRESHOLD = 8;
	/**
	* The number of bits used for generation stamp in sizeCtl.
	* Must be at least 6 for 32bit arrays.
	*/
	private static final int RESIZE_STAMP_BITS = 16;
	/** Number of CPUS, to place bounds on some sizings */
	static final int NCPU = Runtime.getRuntime().availableProcessors();
	/**
	* Minimum number of rebinnings per transfer step. Ranges are
	* subdivided to allow multiple resizer threads.  This value
	* serves as a lower bound to avoid resizers encountering
	* excessive memory contention.  The value should be at least
	* DEFAULT_CAPACITY.
	*/
	private static final int MIN_TRANSFER_STRIDE = 16;
	/**
	* The next table to use; non-null only while resizing.
	*/
	private transient volatile Node&lt;K, V&gt;[] nextTable;
	/**
	* The next table index (plus one) to split while resizing.
	*/
	private transient volatile int transferIndex;
	private static final long TRANSFERINDEX;
	/**
	* The bin count threshold for untreeifying a (split) bin during a
	* resize operation. Should be less than TREEIFY_THRESHOLD, and at
	* most 6 to mesh with shrinkage detection under removal.
	*/
	static final int UNTREEIFY_THRESHOLD = 6;
	static final int HASH_BITS = 0x7fffffff;
	/**
	* The default initial table capacity.  Must be a power of 2
	* (i.e., at least 1) and at most MAXIMUM_CAPACITY.
	*/
	private static final int DEFAULT_CAPACITY = 16;
	private static final int ASHIFT;
	private static final int ABASE;
	/**
	* The maximum number of threads that can help resize.
	* Must fit in 32 - RESIZE_STAMP_BITS bits.
	*/
	private static final int MAX_RESIZERS = (1 &lt;&lt; (32 - RESIZE_STAMP_BITS)) - 1;
	/**
	* The smallest table capacity for which bins may be treeified.
	* (Otherwise the table is resized if too many nodes in a bin.)
	* The value should be at least 4 * TREEIFY_THRESHOLD to avoid
	* conflicts between resizing and treeification thresholds.
	*/
	static final int MIN_TREEIFY_CAPACITY = 64;
	/**
	* Table of counter cells. When non-null, size is a power of 2.
	*/
	private transient volatile CounterCell[] counterCells;
	private static final long BASECOUNT;
	/**
	* Base counter value, used mainly when there is no contention,
	* but also as a fallback during table initialization
	* races. Updated via CAS.
	*/
	private transient volatile long baseCount;
	private static final long CELLVALUE;
	static final int TREEBIN = -2;
	/**
	* Spinlock (locked via CAS) used when resizing and/or creating CounterCells.
	*/
	private transient volatile int cellsBusy;
	private static final long CELLSBUSY;

	Node(int hash, K key, V val) {
	    this.hash = hash;
	    this.key = key;
	    this.val = val;
	}

	Node(int hash, K key, V val, Node&lt;K, V&gt; next) {
	    this(hash, key, val);
	    this.next = next;
	}

    }

    class TreeBin&lt;K, V&gt; extends Node&lt;K, V&gt; {
	/**
	* The largest possible table capacity.  This value must be
	* exactly 1&lt;&lt;30 to stay within Java array allocation and indexing
	* bounds for power of two table sizes, and is further required
	* because the top two bits of 32bit hash fields are used for
	* control purposes.
	*/
	private static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
	/**
	* Table initialization and resizing control.  When negative, the
	* table is being initialized or resized: -1 for initialization,
	* else -(1 + the number of active resizing threads).  Otherwise,
	* when table is null, holds the initial table size to use upon
	* creation, or 0 for default. After initialization, holds the
	* next element count value upon which to resize the table.
	*/
	private transient volatile int sizeCtl;
	/**
	* The array of bins. Lazily initialized upon first insertion.
	* Size is always a power of two. Accessed directly by iterators.
	*/
	transient volatile Node&lt;K, V&gt;[] table;
	private static final Unsafe U = Unsafe.getUnsafe();
	private static final long SIZECTL;
	/**
	* The bit shift for recording size stamp in sizeCtl.
	*/
	private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
	static final int MOVED = -1;
	/**
	* The bin count threshold for using a tree rather than list for a
	* bin.  Bins are converted to trees when adding an element to a
	* bin with at least this many nodes. The value must be greater
	* than 2, and should be at least 8 to mesh with assumptions in
	* tree removal about conversion back to plain bins upon
	* shrinkage.
	*/
	static final int TREEIFY_THRESHOLD = 8;
	/**
	* The number of bits used for generation stamp in sizeCtl.
	* Must be at least 6 for 32bit arrays.
	*/
	private static final int RESIZE_STAMP_BITS = 16;
	/** Number of CPUS, to place bounds on some sizings */
	static final int NCPU = Runtime.getRuntime().availableProcessors();
	/**
	* Minimum number of rebinnings per transfer step. Ranges are
	* subdivided to allow multiple resizer threads.  This value
	* serves as a lower bound to avoid resizers encountering
	* excessive memory contention.  The value should be at least
	* DEFAULT_CAPACITY.
	*/
	private static final int MIN_TRANSFER_STRIDE = 16;
	/**
	* The next table to use; non-null only while resizing.
	*/
	private transient volatile Node&lt;K, V&gt;[] nextTable;
	/**
	* The next table index (plus one) to split while resizing.
	*/
	private transient volatile int transferIndex;
	private static final long TRANSFERINDEX;
	/**
	* The bin count threshold for untreeifying a (split) bin during a
	* resize operation. Should be less than TREEIFY_THRESHOLD, and at
	* most 6 to mesh with shrinkage detection under removal.
	*/
	static final int UNTREEIFY_THRESHOLD = 6;
	static final int HASH_BITS = 0x7fffffff;
	/**
	* The default initial table capacity.  Must be a power of 2
	* (i.e., at least 1) and at most MAXIMUM_CAPACITY.
	*/
	private static final int DEFAULT_CAPACITY = 16;
	private static final int ASHIFT;
	private static final int ABASE;
	/**
	* The maximum number of threads that can help resize.
	* Must fit in 32 - RESIZE_STAMP_BITS bits.
	*/
	private static final int MAX_RESIZERS = (1 &lt;&lt; (32 - RESIZE_STAMP_BITS)) - 1;
	/**
	* The smallest table capacity for which bins may be treeified.
	* (Otherwise the table is resized if too many nodes in a bin.)
	* The value should be at least 4 * TREEIFY_THRESHOLD to avoid
	* conflicts between resizing and treeification thresholds.
	*/
	static final int MIN_TREEIFY_CAPACITY = 64;
	/**
	* Table of counter cells. When non-null, size is a power of 2.
	*/
	private transient volatile CounterCell[] counterCells;
	private static final long BASECOUNT;
	/**
	* Base counter value, used mainly when there is no contention,
	* but also as a fallback during table initialization
	* races. Updated via CAS.
	*/
	private transient volatile long baseCount;
	private static final long CELLVALUE;
	static final int TREEBIN = -2;
	/**
	* Spinlock (locked via CAS) used when resizing and/or creating CounterCells.
	*/
	private transient volatile int cellsBusy;
	private static final long CELLSBUSY;

	/**
	 * Finds or adds a node.
	 * @return null if added
	 */
	final TreeNode&lt;K, V&gt; putTreeVal(int h, K k, V v) {
	    Class&lt;?&gt; kc = null;
	    boolean searched = false;
	    for (TreeNode&lt;K, V&gt; p = root;;) {
		int dir, ph;
		K pk;
		if (p == null) {
		    first = root = new TreeNode&lt;K, V&gt;(h, k, v, null, null);
		    break;
		} else if ((ph = p.hash) &gt; h)
		    dir = -1;
		else if (ph &lt; h)
		    dir = 1;
		else if ((pk = p.key) == k || (pk != null && k.equals(pk)))
		    return p;
		else if ((kc == null && (kc = comparableClassFor(k)) == null)
			|| (dir = compareComparables(kc, k, pk)) == 0) {
		    if (!searched) {
			TreeNode&lt;K, V&gt; q, ch;
			searched = true;
			if (((ch = p.left) != null && (q = ch.findTreeNode(h, k, kc)) != null)
				|| ((ch = p.right) != null && (q = ch.findTreeNode(h, k, kc)) != null))
			    return q;
		    }
		    dir = tieBreakOrder(k, pk);
		}

		TreeNode&lt;K, V&gt; xp = p;
		if ((p = (dir &lt;= 0) ? p.left : p.right) == null) {
		    TreeNode&lt;K, V&gt; x, f = first;
		    first = x = new TreeNode&lt;K, V&gt;(h, k, v, f, xp);
		    if (f != null)
			f.prev = x;
		    if (dir &lt;= 0)
			xp.left = x;
		    else
			xp.right = x;
		    if (!xp.red)
			x.red = true;
		    else {
			lockRoot();
			try {
			    root = balanceInsertion(root, x);
			} finally {
			    unlockRoot();
			}
		    }
		    break;
		}
	    }
	    assert checkInvariants(root);
	    return null;
	}

	/**
	 * Creates bin with initial set of nodes headed by b.
	 */
	TreeBin(TreeNode&lt;K, V&gt; b) {
	    super(TREEBIN, null, null);
	    this.first = b;
	    TreeNode&lt;K, V&gt; r = null;
	    for (TreeNode&lt;K, V&gt; x = b, next; x != null; x = next) {
		next = (TreeNode&lt;K, V&gt;) x.next;
		x.left = x.right = null;
		if (r == null) {
		    x.parent = null;
		    x.red = false;
		    r = x;
		} else {
		    K k = x.key;
		    int h = x.hash;
		    Class&lt;?&gt; kc = null;
		    for (TreeNode&lt;K, V&gt; p = r;;) {
			int dir, ph;
			K pk = p.key;
			if ((ph = p.hash) &gt; h)
			    dir = -1;
			else if (ph &lt; h)
			    dir = 1;
			else if ((kc == null && (kc = comparableClassFor(k)) == null)
				|| (dir = compareComparables(kc, k, pk)) == 0)
			    dir = tieBreakOrder(k, pk);
			TreeNode&lt;K, V&gt; xp = p;
			if ((p = (dir &lt;= 0) ? p.left : p.right) == null) {
			    x.parent = xp;
			    if (dir &lt;= 0)
				xp.left = x;
			    else
				xp.right = x;
			    r = balanceInsertion(r, x);
			    break;
			}
		    }
		}
	    }
	    this.root = r;
	    assert checkInvariants(root);
	}

	/**
	 * Tie-breaking utility for ordering insertions when equal
	 * hashCodes and non-comparable. We don't require a total
	 * order, just a consistent insertion rule to maintain
	 * equivalence across rebalancings. Tie-breaking further than
	 * necessary simplifies testing a bit.
	 */
	static int tieBreakOrder(Object a, Object b) {
	    int d;
	    if (a == null || b == null || (d = a.getClass().getName().compareTo(b.getClass().getName())) == 0)
		d = (System.identityHashCode(a) &lt;= System.identityHashCode(b) ? -1 : 1);
	    return d;
	}

	/**
	 * Acquires write lock for tree restructuring.
	 */
	private final void lockRoot() {
	    if (!U.compareAndSetInt(this, LOCKSTATE, 0, WRITER))
		contendedLock(); // offload to separate method
	}

	static &lt;K, V&gt; TreeNode&lt;K, V&gt; balanceInsertion(TreeNode&lt;K, V&gt; root, TreeNode&lt;K, V&gt; x) {
	    x.red = true;
	    for (TreeNode&lt;K, V&gt; xp, xpp, xppl, xppr;;) {
		if ((xp = x.parent) == null) {
		    x.red = false;
		    return x;
		} else if (!xp.red || (xpp = xp.parent) == null)
		    return root;
		if (xp == (xppl = xpp.left)) {
		    if ((xppr = xpp.right) != null && xppr.red) {
			xppr.red = false;
			xp.red = false;
			xpp.red = true;
			x = xpp;
		    } else {
			if (x == xp.right) {
			    root = rotateLeft(root, x = xp);
			    xpp = (xp = x.parent) == null ? null : xp.parent;
			}
			if (xp != null) {
			    xp.red = false;
			    if (xpp != null) {
				xpp.red = true;
				root = rotateRight(root, xpp);
			    }
			}
		    }
		} else {
		    if (xppl != null && xppl.red) {
			xppl.red = false;
			xp.red = false;
			xpp.red = true;
			x = xpp;
		    } else {
			if (x == xp.left) {
			    root = rotateRight(root, x = xp);
			    xpp = (xp = x.parent) == null ? null : xp.parent;
			}
			if (xp != null) {
			    xp.red = false;
			    if (xpp != null) {
				xpp.red = true;
				root = rotateLeft(root, xpp);
			    }
			}
		    }
		}
	    }
	}

	/**
	 * Releases write lock for tree restructuring.
	 */
	private final void unlockRoot() {
	    lockState = 0;
	}

	/**
	 * Checks invariants recursively for the tree of Nodes rooted at t.
	 */
	static &lt;K, V&gt; boolean checkInvariants(TreeNode&lt;K, V&gt; t) {
	    TreeNode&lt;K, V&gt; tp = t.parent, tl = t.left, tr = t.right, tb = t.prev, tn = (TreeNode&lt;K, V&gt;) t.next;
	    if (tb != null && tb.next != t)
		return false;
	    if (tn != null && tn.prev != t)
		return false;
	    if (tp != null && t != tp.left && t != tp.right)
		return false;
	    if (tl != null && (tl.parent != t || tl.hash &gt; t.hash))
		return false;
	    if (tr != null && (tr.parent != t || tr.hash &lt; t.hash))
		return false;
	    if (t.red && tl != null && tl.red && tr != null && tr.red)
		return false;
	    if (tl != null && !checkInvariants(tl))
		return false;
	    if (tr != null && !checkInvariants(tr))
		return false;
	    return true;
	}

	/**
	 * Possibly blocks awaiting root lock.
	 */
	private final void contendedLock() {
	    boolean waiting = false;
	    for (int s;;) {
		if (((s = lockState) & ~WAITER) == 0) {
		    if (U.compareAndSetInt(this, LOCKSTATE, s, WRITER)) {
			if (waiting)
			    waiter = null;
			return;
		    }
		} else if ((s & WAITER) == 0) {
		    if (U.compareAndSetInt(this, LOCKSTATE, s, s | WAITER)) {
			waiting = true;
			waiter = Thread.currentThread();
		    }
		} else if (waiting)
		    LockSupport.park(this);
	    }
	}

	static &lt;K, V&gt; TreeNode&lt;K, V&gt; rotateLeft(TreeNode&lt;K, V&gt; root, TreeNode&lt;K, V&gt; p) {
	    TreeNode&lt;K, V&gt; r, pp, rl;
	    if (p != null && (r = p.right) != null) {
		if ((rl = p.right = r.left) != null)
		    rl.parent = p;
		if ((pp = r.parent = p.parent) == null)
		    (root = r).red = false;
		else if (pp.left == p)
		    pp.left = r;
		else
		    pp.right = r;
		r.left = p;
		p.parent = r;
	    }
	    return root;
	}

	static &lt;K, V&gt; TreeNode&lt;K, V&gt; rotateRight(TreeNode&lt;K, V&gt; root, TreeNode&lt;K, V&gt; p) {
	    TreeNode&lt;K, V&gt; l, pp, lr;
	    if (p != null && (l = p.left) != null) {
		if ((lr = p.left = l.right) != null)
		    lr.parent = p;
		if ((pp = l.parent = p.parent) == null)
		    (root = l).red = false;
		else if (pp.right == p)
		    pp.right = l;
		else
		    pp.left = l;
		l.right = p;
		p.parent = l;
	    }
	    return root;
	}

    }

    class ForwardingNode&lt;K, V&gt; extends Node&lt;K, V&gt; {
	/**
	* The largest possible table capacity.  This value must be
	* exactly 1&lt;&lt;30 to stay within Java array allocation and indexing
	* bounds for power of two table sizes, and is further required
	* because the top two bits of 32bit hash fields are used for
	* control purposes.
	*/
	private static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
	/**
	* Table initialization and resizing control.  When negative, the
	* table is being initialized or resized: -1 for initialization,
	* else -(1 + the number of active resizing threads).  Otherwise,
	* when table is null, holds the initial table size to use upon
	* creation, or 0 for default. After initialization, holds the
	* next element count value upon which to resize the table.
	*/
	private transient volatile int sizeCtl;
	/**
	* The array of bins. Lazily initialized upon first insertion.
	* Size is always a power of two. Accessed directly by iterators.
	*/
	transient volatile Node&lt;K, V&gt;[] table;
	private static final Unsafe U = Unsafe.getUnsafe();
	private static final long SIZECTL;
	/**
	* The bit shift for recording size stamp in sizeCtl.
	*/
	private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
	static final int MOVED = -1;
	/**
	* The bin count threshold for using a tree rather than list for a
	* bin.  Bins are converted to trees when adding an element to a
	* bin with at least this many nodes. The value must be greater
	* than 2, and should be at least 8 to mesh with assumptions in
	* tree removal about conversion back to plain bins upon
	* shrinkage.
	*/
	static final int TREEIFY_THRESHOLD = 8;
	/**
	* The number of bits used for generation stamp in sizeCtl.
	* Must be at least 6 for 32bit arrays.
	*/
	private static final int RESIZE_STAMP_BITS = 16;
	/** Number of CPUS, to place bounds on some sizings */
	static final int NCPU = Runtime.getRuntime().availableProcessors();
	/**
	* Minimum number of rebinnings per transfer step. Ranges are
	* subdivided to allow multiple resizer threads.  This value
	* serves as a lower bound to avoid resizers encountering
	* excessive memory contention.  The value should be at least
	* DEFAULT_CAPACITY.
	*/
	private static final int MIN_TRANSFER_STRIDE = 16;
	/**
	* The next table to use; non-null only while resizing.
	*/
	private transient volatile Node&lt;K, V&gt;[] nextTable;
	/**
	* The next table index (plus one) to split while resizing.
	*/
	private transient volatile int transferIndex;
	private static final long TRANSFERINDEX;
	/**
	* The bin count threshold for untreeifying a (split) bin during a
	* resize operation. Should be less than TREEIFY_THRESHOLD, and at
	* most 6 to mesh with shrinkage detection under removal.
	*/
	static final int UNTREEIFY_THRESHOLD = 6;
	static final int HASH_BITS = 0x7fffffff;
	/**
	* The default initial table capacity.  Must be a power of 2
	* (i.e., at least 1) and at most MAXIMUM_CAPACITY.
	*/
	private static final int DEFAULT_CAPACITY = 16;
	private static final int ASHIFT;
	private static final int ABASE;
	/**
	* The maximum number of threads that can help resize.
	* Must fit in 32 - RESIZE_STAMP_BITS bits.
	*/
	private static final int MAX_RESIZERS = (1 &lt;&lt; (32 - RESIZE_STAMP_BITS)) - 1;
	/**
	* The smallest table capacity for which bins may be treeified.
	* (Otherwise the table is resized if too many nodes in a bin.)
	* The value should be at least 4 * TREEIFY_THRESHOLD to avoid
	* conflicts between resizing and treeification thresholds.
	*/
	static final int MIN_TREEIFY_CAPACITY = 64;
	/**
	* Table of counter cells. When non-null, size is a power of 2.
	*/
	private transient volatile CounterCell[] counterCells;
	private static final long BASECOUNT;
	/**
	* Base counter value, used mainly when there is no contention,
	* but also as a fallback during table initialization
	* races. Updated via CAS.
	*/
	private transient volatile long baseCount;
	private static final long CELLVALUE;
	static final int TREEBIN = -2;
	/**
	* Spinlock (locked via CAS) used when resizing and/or creating CounterCells.
	*/
	private transient volatile int cellsBusy;
	private static final long CELLSBUSY;

	ForwardingNode(Node&lt;K, V&gt;[] tab) {
	    super(MOVED, null, null);
	    this.nextTable = tab;
	}

    }

    class TreeNode&lt;K, V&gt; extends Node&lt;K, V&gt; {
	/**
	* The largest possible table capacity.  This value must be
	* exactly 1&lt;&lt;30 to stay within Java array allocation and indexing
	* bounds for power of two table sizes, and is further required
	* because the top two bits of 32bit hash fields are used for
	* control purposes.
	*/
	private static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
	/**
	* Table initialization and resizing control.  When negative, the
	* table is being initialized or resized: -1 for initialization,
	* else -(1 + the number of active resizing threads).  Otherwise,
	* when table is null, holds the initial table size to use upon
	* creation, or 0 for default. After initialization, holds the
	* next element count value upon which to resize the table.
	*/
	private transient volatile int sizeCtl;
	/**
	* The array of bins. Lazily initialized upon first insertion.
	* Size is always a power of two. Accessed directly by iterators.
	*/
	transient volatile Node&lt;K, V&gt;[] table;
	private static final Unsafe U = Unsafe.getUnsafe();
	private static final long SIZECTL;
	/**
	* The bit shift for recording size stamp in sizeCtl.
	*/
	private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
	static final int MOVED = -1;
	/**
	* The bin count threshold for using a tree rather than list for a
	* bin.  Bins are converted to trees when adding an element to a
	* bin with at least this many nodes. The value must be greater
	* than 2, and should be at least 8 to mesh with assumptions in
	* tree removal about conversion back to plain bins upon
	* shrinkage.
	*/
	static final int TREEIFY_THRESHOLD = 8;
	/**
	* The number of bits used for generation stamp in sizeCtl.
	* Must be at least 6 for 32bit arrays.
	*/
	private static final int RESIZE_STAMP_BITS = 16;
	/** Number of CPUS, to place bounds on some sizings */
	static final int NCPU = Runtime.getRuntime().availableProcessors();
	/**
	* Minimum number of rebinnings per transfer step. Ranges are
	* subdivided to allow multiple resizer threads.  This value
	* serves as a lower bound to avoid resizers encountering
	* excessive memory contention.  The value should be at least
	* DEFAULT_CAPACITY.
	*/
	private static final int MIN_TRANSFER_STRIDE = 16;
	/**
	* The next table to use; non-null only while resizing.
	*/
	private transient volatile Node&lt;K, V&gt;[] nextTable;
	/**
	* The next table index (plus one) to split while resizing.
	*/
	private transient volatile int transferIndex;
	private static final long TRANSFERINDEX;
	/**
	* The bin count threshold for untreeifying a (split) bin during a
	* resize operation. Should be less than TREEIFY_THRESHOLD, and at
	* most 6 to mesh with shrinkage detection under removal.
	*/
	static final int UNTREEIFY_THRESHOLD = 6;
	static final int HASH_BITS = 0x7fffffff;
	/**
	* The default initial table capacity.  Must be a power of 2
	* (i.e., at least 1) and at most MAXIMUM_CAPACITY.
	*/
	private static final int DEFAULT_CAPACITY = 16;
	private static final int ASHIFT;
	private static final int ABASE;
	/**
	* The maximum number of threads that can help resize.
	* Must fit in 32 - RESIZE_STAMP_BITS bits.
	*/
	private static final int MAX_RESIZERS = (1 &lt;&lt; (32 - RESIZE_STAMP_BITS)) - 1;
	/**
	* The smallest table capacity for which bins may be treeified.
	* (Otherwise the table is resized if too many nodes in a bin.)
	* The value should be at least 4 * TREEIFY_THRESHOLD to avoid
	* conflicts between resizing and treeification thresholds.
	*/
	static final int MIN_TREEIFY_CAPACITY = 64;
	/**
	* Table of counter cells. When non-null, size is a power of 2.
	*/
	private transient volatile CounterCell[] counterCells;
	private static final long BASECOUNT;
	/**
	* Base counter value, used mainly when there is no contention,
	* but also as a fallback during table initialization
	* races. Updated via CAS.
	*/
	private transient volatile long baseCount;
	private static final long CELLVALUE;
	static final int TREEBIN = -2;
	/**
	* Spinlock (locked via CAS) used when resizing and/or creating CounterCells.
	*/
	private transient volatile int cellsBusy;
	private static final long CELLSBUSY;

	TreeNode(int hash, K key, V val, Node&lt;K, V&gt; next, TreeNode&lt;K, V&gt; parent) {
	    super(hash, key, val, next);
	    this.parent = parent;
	}

	/**
	 * Returns the TreeNode (or null if not found) for the given key
	 * starting at given root.
	 */
	final TreeNode&lt;K, V&gt; findTreeNode(int h, Object k, Class&lt;?&gt; kc) {
	    if (k != null) {
		TreeNode&lt;K, V&gt; p = this;
		do {
		    int ph, dir;
		    K pk;
		    TreeNode&lt;K, V&gt; q;
		    TreeNode&lt;K, V&gt; pl = p.left, pr = p.right;
		    if ((ph = p.hash) &gt; h)
			p = pl;
		    else if (ph &lt; h)
			p = pr;
		    else if ((pk = p.key) == k || (pk != null && k.equals(pk)))
			return p;
		    else if (pl == null)
			p = pr;
		    else if (pr == null)
			p = pl;
		    else if ((kc != null || (kc = comparableClassFor(k)) != null)
			    && (dir = compareComparables(kc, k, pk)) != 0)
			p = (dir &lt; 0) ? pl : pr;
		    else if ((q = pr.findTreeNode(h, k, kc)) != null)
			return q;
		    else
			p = pl;
		} while (p != null);
	    }
	    return null;
	}

    }

    class CounterCell {
	/**
	* The largest possible table capacity.  This value must be
	* exactly 1&lt;&lt;30 to stay within Java array allocation and indexing
	* bounds for power of two table sizes, and is further required
	* because the top two bits of 32bit hash fields are used for
	* control purposes.
	*/
	private static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
	/**
	* Table initialization and resizing control.  When negative, the
	* table is being initialized or resized: -1 for initialization,
	* else -(1 + the number of active resizing threads).  Otherwise,
	* when table is null, holds the initial table size to use upon
	* creation, or 0 for default. After initialization, holds the
	* next element count value upon which to resize the table.
	*/
	private transient volatile int sizeCtl;
	/**
	* The array of bins. Lazily initialized upon first insertion.
	* Size is always a power of two. Accessed directly by iterators.
	*/
	transient volatile Node&lt;K, V&gt;[] table;
	private static final Unsafe U = Unsafe.getUnsafe();
	private static final long SIZECTL;
	/**
	* The bit shift for recording size stamp in sizeCtl.
	*/
	private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
	static final int MOVED = -1;
	/**
	* The bin count threshold for using a tree rather than list for a
	* bin.  Bins are converted to trees when adding an element to a
	* bin with at least this many nodes. The value must be greater
	* than 2, and should be at least 8 to mesh with assumptions in
	* tree removal about conversion back to plain bins upon
	* shrinkage.
	*/
	static final int TREEIFY_THRESHOLD = 8;
	/**
	* The number of bits used for generation stamp in sizeCtl.
	* Must be at least 6 for 32bit arrays.
	*/
	private static final int RESIZE_STAMP_BITS = 16;
	/** Number of CPUS, to place bounds on some sizings */
	static final int NCPU = Runtime.getRuntime().availableProcessors();
	/**
	* Minimum number of rebinnings per transfer step. Ranges are
	* subdivided to allow multiple resizer threads.  This value
	* serves as a lower bound to avoid resizers encountering
	* excessive memory contention.  The value should be at least
	* DEFAULT_CAPACITY.
	*/
	private static final int MIN_TRANSFER_STRIDE = 16;
	/**
	* The next table to use; non-null only while resizing.
	*/
	private transient volatile Node&lt;K, V&gt;[] nextTable;
	/**
	* The next table index (plus one) to split while resizing.
	*/
	private transient volatile int transferIndex;
	private static final long TRANSFERINDEX;
	/**
	* The bin count threshold for untreeifying a (split) bin during a
	* resize operation. Should be less than TREEIFY_THRESHOLD, and at
	* most 6 to mesh with shrinkage detection under removal.
	*/
	static final int UNTREEIFY_THRESHOLD = 6;
	static final int HASH_BITS = 0x7fffffff;
	/**
	* The default initial table capacity.  Must be a power of 2
	* (i.e., at least 1) and at most MAXIMUM_CAPACITY.
	*/
	private static final int DEFAULT_CAPACITY = 16;
	private static final int ASHIFT;
	private static final int ABASE;
	/**
	* The maximum number of threads that can help resize.
	* Must fit in 32 - RESIZE_STAMP_BITS bits.
	*/
	private static final int MAX_RESIZERS = (1 &lt;&lt; (32 - RESIZE_STAMP_BITS)) - 1;
	/**
	* The smallest table capacity for which bins may be treeified.
	* (Otherwise the table is resized if too many nodes in a bin.)
	* The value should be at least 4 * TREEIFY_THRESHOLD to avoid
	* conflicts between resizing and treeification thresholds.
	*/
	static final int MIN_TREEIFY_CAPACITY = 64;
	/**
	* Table of counter cells. When non-null, size is a power of 2.
	*/
	private transient volatile CounterCell[] counterCells;
	private static final long BASECOUNT;
	/**
	* Base counter value, used mainly when there is no contention,
	* but also as a fallback during table initialization
	* races. Updated via CAS.
	*/
	private transient volatile long baseCount;
	private static final long CELLVALUE;
	static final int TREEBIN = -2;
	/**
	* Spinlock (locked via CAS) used when resizing and/or creating CounterCells.
	*/
	private transient volatile int cellsBusy;
	private static final long CELLSBUSY;

	CounterCell(long x) {
	    value = x;
	}

    }

}

