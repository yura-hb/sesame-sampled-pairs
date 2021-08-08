import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import jdk.internal.misc.Unsafe;

class ConcurrentHashMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    /**
     * Removes all of the mappings from this map.
     */
    public void clear() {
	long delta = 0L; // negative number of deletions
	int i = 0;
	Node&lt;K, V&gt;[] tab = table;
	while (tab != null && i &lt; tab.length) {
	    int fh;
	    Node&lt;K, V&gt; f = tabAt(tab, i);
	    if (f == null)
		++i;
	    else if ((fh = f.hash) == MOVED) {
		tab = helpTransfer(tab, f);
		i = 0; // restart
	    } else {
		synchronized (f) {
		    if (tabAt(tab, i) == f) {
			Node&lt;K, V&gt; p = (fh &gt;= 0 ? f : (f instanceof TreeBin) ? ((TreeBin&lt;K, V&gt;) f).first : null);
			while (p != null) {
			    --delta;
			    p = p.next;
			}
			setTabAt(tab, i++, null);
		    }
		}
	    }
	}
	if (delta != 0L)
	    addCount(delta, -1);
    }

    /**
     * The array of bins. Lazily initialized upon first insertion.
     * Size is always a power of two. Accessed directly by iterators.
     */
    transient volatile Node&lt;K, V&gt;[] table;
    static final int MOVED = -1;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final int ASHIFT;
    private static final int ABASE;
    /**
     * The next table to use; non-null only while resizing.
     */
    private transient volatile Node&lt;K, V&gt;[] nextTable;
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
     * The bit shift for recording size stamp in sizeCtl.
     */
    private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
    /**
     * The maximum number of threads that can help resize.
     * Must fit in 32 - RESIZE_STAMP_BITS bits.
     */
    private static final int MAX_RESIZERS = (1 &lt;&lt; (32 - RESIZE_STAMP_BITS)) - 1;
    /**
     * The next table index (plus one) to split while resizing.
     */
    private transient volatile int transferIndex;
    private static final long SIZECTL;
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
    /**
     * The largest possible table capacity.  This value must be
     * exactly 1&lt;&lt;30 to stay within Java array allocation and indexing
     * bounds for power of two table sizes, and is further required
     * because the top two bits of 32bit hash fields are used for
     * control purposes.
     */
    private static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
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
    private static final long TRANSFERINDEX;
    /**
     * The bin count threshold for untreeifying a (split) bin during a
     * resize operation. Should be less than TREEIFY_THRESHOLD, and at
     * most 6 to mesh with shrinkage detection under removal.
     */
    static final int UNTREEIFY_THRESHOLD = 6;
    /**
     * Spinlock (locked via CAS) used when resizing and/or creating CounterCells.
     */
    private transient volatile int cellsBusy;
    private static final long CELLSBUSY;
    static final int TREEBIN = -2;

    @SuppressWarnings("unchecked")
    static final &lt;K, V&gt; Node&lt;K, V&gt; tabAt(Node&lt;K, V&gt;[] tab, int i) {
	return (Node&lt;K, V&gt;) U.getObjectAcquire(tab, ((long) i &lt;&lt; ASHIFT) + ABASE);
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

    static final &lt;K, V&gt; void setTabAt(Node&lt;K, V&gt;[] tab, int i, Node&lt;K, V&gt; v) {
	U.putObjectRelease(tab, ((long) i &lt;&lt; ASHIFT) + ABASE, v);
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

    static final &lt;K, V&gt; boolean casTabAt(Node&lt;K, V&gt;[] tab, int i, Node&lt;K, V&gt; c, Node&lt;K, V&gt; v) {
	return U.compareAndSetObject(tab, ((long) i &lt;&lt; ASHIFT) + ABASE, c, v);
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

    class ForwardingNode&lt;K, V&gt; extends Node&lt;K, V&gt; {
	/**
	* The array of bins. Lazily initialized upon first insertion.
	* Size is always a power of two. Accessed directly by iterators.
	*/
	transient volatile Node&lt;K, V&gt;[] table;
	static final int MOVED = -1;
	private static final Unsafe U = Unsafe.getUnsafe();
	private static final int ASHIFT;
	private static final int ABASE;
	/**
	* The next table to use; non-null only while resizing.
	*/
	private transient volatile Node&lt;K, V&gt;[] nextTable;
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
	* The bit shift for recording size stamp in sizeCtl.
	*/
	private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
	/**
	* The maximum number of threads that can help resize.
	* Must fit in 32 - RESIZE_STAMP_BITS bits.
	*/
	private static final int MAX_RESIZERS = (1 &lt;&lt; (32 - RESIZE_STAMP_BITS)) - 1;
	/**
	* The next table index (plus one) to split while resizing.
	*/
	private transient volatile int transferIndex;
	private static final long SIZECTL;
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
	/**
	* The largest possible table capacity.  This value must be
	* exactly 1&lt;&lt;30 to stay within Java array allocation and indexing
	* bounds for power of two table sizes, and is further required
	* because the top two bits of 32bit hash fields are used for
	* control purposes.
	*/
	private static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
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
	private static final long TRANSFERINDEX;
	/**
	* The bin count threshold for untreeifying a (split) bin during a
	* resize operation. Should be less than TREEIFY_THRESHOLD, and at
	* most 6 to mesh with shrinkage detection under removal.
	*/
	static final int UNTREEIFY_THRESHOLD = 6;
	/**
	* Spinlock (locked via CAS) used when resizing and/or creating CounterCells.
	*/
	private transient volatile int cellsBusy;
	private static final long CELLSBUSY;
	static final int TREEBIN = -2;

	ForwardingNode(Node&lt;K, V&gt;[] tab) {
	    super(MOVED, null, null);
	    this.nextTable = tab;
	}

    }

    class Node&lt;K, V&gt; implements Entry&lt;K, V&gt; {
	/**
	* The array of bins. Lazily initialized upon first insertion.
	* Size is always a power of two. Accessed directly by iterators.
	*/
	transient volatile Node&lt;K, V&gt;[] table;
	static final int MOVED = -1;
	private static final Unsafe U = Unsafe.getUnsafe();
	private static final int ASHIFT;
	private static final int ABASE;
	/**
	* The next table to use; non-null only while resizing.
	*/
	private transient volatile Node&lt;K, V&gt;[] nextTable;
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
	* The bit shift for recording size stamp in sizeCtl.
	*/
	private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
	/**
	* The maximum number of threads that can help resize.
	* Must fit in 32 - RESIZE_STAMP_BITS bits.
	*/
	private static final int MAX_RESIZERS = (1 &lt;&lt; (32 - RESIZE_STAMP_BITS)) - 1;
	/**
	* The next table index (plus one) to split while resizing.
	*/
	private transient volatile int transferIndex;
	private static final long SIZECTL;
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
	/**
	* The largest possible table capacity.  This value must be
	* exactly 1&lt;&lt;30 to stay within Java array allocation and indexing
	* bounds for power of two table sizes, and is further required
	* because the top two bits of 32bit hash fields are used for
	* control purposes.
	*/
	private static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
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
	private static final long TRANSFERINDEX;
	/**
	* The bin count threshold for untreeifying a (split) bin during a
	* resize operation. Should be less than TREEIFY_THRESHOLD, and at
	* most 6 to mesh with shrinkage detection under removal.
	*/
	static final int UNTREEIFY_THRESHOLD = 6;
	/**
	* Spinlock (locked via CAS) used when resizing and/or creating CounterCells.
	*/
	private transient volatile int cellsBusy;
	private static final long CELLSBUSY;
	static final int TREEBIN = -2;

	Node(int hash, K key, V val, Node&lt;K, V&gt; next) {
	    this(hash, key, val);
	    this.next = next;
	}

	Node(int hash, K key, V val) {
	    this.hash = hash;
	    this.key = key;
	    this.val = val;
	}

    }

    class TreeNode&lt;K, V&gt; extends Node&lt;K, V&gt; {
	/**
	* The array of bins. Lazily initialized upon first insertion.
	* Size is always a power of two. Accessed directly by iterators.
	*/
	transient volatile Node&lt;K, V&gt;[] table;
	static final int MOVED = -1;
	private static final Unsafe U = Unsafe.getUnsafe();
	private static final int ASHIFT;
	private static final int ABASE;
	/**
	* The next table to use; non-null only while resizing.
	*/
	private transient volatile Node&lt;K, V&gt;[] nextTable;
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
	* The bit shift for recording size stamp in sizeCtl.
	*/
	private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
	/**
	* The maximum number of threads that can help resize.
	* Must fit in 32 - RESIZE_STAMP_BITS bits.
	*/
	private static final int MAX_RESIZERS = (1 &lt;&lt; (32 - RESIZE_STAMP_BITS)) - 1;
	/**
	* The next table index (plus one) to split while resizing.
	*/
	private transient volatile int transferIndex;
	private static final long SIZECTL;
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
	/**
	* The largest possible table capacity.  This value must be
	* exactly 1&lt;&lt;30 to stay within Java array allocation and indexing
	* bounds for power of two table sizes, and is further required
	* because the top two bits of 32bit hash fields are used for
	* control purposes.
	*/
	private static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
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
	private static final long TRANSFERINDEX;
	/**
	* The bin count threshold for untreeifying a (split) bin during a
	* resize operation. Should be less than TREEIFY_THRESHOLD, and at
	* most 6 to mesh with shrinkage detection under removal.
	*/
	static final int UNTREEIFY_THRESHOLD = 6;
	/**
	* Spinlock (locked via CAS) used when resizing and/or creating CounterCells.
	*/
	private transient volatile int cellsBusy;
	private static final long CELLSBUSY;
	static final int TREEBIN = -2;

	TreeNode(int hash, K key, V val, Node&lt;K, V&gt; next, TreeNode&lt;K, V&gt; parent) {
	    super(hash, key, val, next);
	    this.parent = parent;
	}

    }

    class TreeBin&lt;K, V&gt; extends Node&lt;K, V&gt; {
	/**
	* The array of bins. Lazily initialized upon first insertion.
	* Size is always a power of two. Accessed directly by iterators.
	*/
	transient volatile Node&lt;K, V&gt;[] table;
	static final int MOVED = -1;
	private static final Unsafe U = Unsafe.getUnsafe();
	private static final int ASHIFT;
	private static final int ABASE;
	/**
	* The next table to use; non-null only while resizing.
	*/
	private transient volatile Node&lt;K, V&gt;[] nextTable;
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
	* The bit shift for recording size stamp in sizeCtl.
	*/
	private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
	/**
	* The maximum number of threads that can help resize.
	* Must fit in 32 - RESIZE_STAMP_BITS bits.
	*/
	private static final int MAX_RESIZERS = (1 &lt;&lt; (32 - RESIZE_STAMP_BITS)) - 1;
	/**
	* The next table index (plus one) to split while resizing.
	*/
	private transient volatile int transferIndex;
	private static final long SIZECTL;
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
	/**
	* The largest possible table capacity.  This value must be
	* exactly 1&lt;&lt;30 to stay within Java array allocation and indexing
	* bounds for power of two table sizes, and is further required
	* because the top two bits of 32bit hash fields are used for
	* control purposes.
	*/
	private static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
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
	private static final long TRANSFERINDEX;
	/**
	* The bin count threshold for untreeifying a (split) bin during a
	* resize operation. Should be less than TREEIFY_THRESHOLD, and at
	* most 6 to mesh with shrinkage detection under removal.
	*/
	static final int UNTREEIFY_THRESHOLD = 6;
	/**
	* Spinlock (locked via CAS) used when resizing and/or creating CounterCells.
	*/
	private transient volatile int cellsBusy;
	private static final long CELLSBUSY;
	static final int TREEBIN = -2;

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

    class CounterCell {
	/**
	* The array of bins. Lazily initialized upon first insertion.
	* Size is always a power of two. Accessed directly by iterators.
	*/
	transient volatile Node&lt;K, V&gt;[] table;
	static final int MOVED = -1;
	private static final Unsafe U = Unsafe.getUnsafe();
	private static final int ASHIFT;
	private static final int ABASE;
	/**
	* The next table to use; non-null only while resizing.
	*/
	private transient volatile Node&lt;K, V&gt;[] nextTable;
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
	* The bit shift for recording size stamp in sizeCtl.
	*/
	private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;
	/**
	* The maximum number of threads that can help resize.
	* Must fit in 32 - RESIZE_STAMP_BITS bits.
	*/
	private static final int MAX_RESIZERS = (1 &lt;&lt; (32 - RESIZE_STAMP_BITS)) - 1;
	/**
	* The next table index (plus one) to split while resizing.
	*/
	private transient volatile int transferIndex;
	private static final long SIZECTL;
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
	/**
	* The largest possible table capacity.  This value must be
	* exactly 1&lt;&lt;30 to stay within Java array allocation and indexing
	* bounds for power of two table sizes, and is further required
	* because the top two bits of 32bit hash fields are used for
	* control purposes.
	*/
	private static final int MAXIMUM_CAPACITY = 1 &lt;&lt; 30;
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
	private static final long TRANSFERINDEX;
	/**
	* The bin count threshold for untreeifying a (split) bin during a
	* resize operation. Should be less than TREEIFY_THRESHOLD, and at
	* most 6 to mesh with shrinkage detection under removal.
	*/
	static final int UNTREEIFY_THRESHOLD = 6;
	/**
	* Spinlock (locked via CAS) used when resizing and/or creating CounterCells.
	*/
	private transient volatile int cellsBusy;
	private static final long CELLSBUSY;
	static final int TREEBIN = -2;

	CounterCell(long x) {
	    value = x;
	}

    }

}

