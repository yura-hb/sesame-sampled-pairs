import java.lang.reflect.ParameterizedType;
import java.util.concurrent.locks.LockSupport;
import jdk.internal.misc.Unsafe;

class ConcurrentHashMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    class TreeBin&lt;K, V&gt; extends Node&lt;K, V&gt; {
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

	TreeNode&lt;K, V&gt; root;
	volatile TreeNode&lt;K, V&gt; first;
	private static final Unsafe U = Unsafe.getUnsafe();
	private static final long LOCKSTATE = U.objectFieldOffset(TreeBin.class, "lockState");
	static final int WRITER = 1;
	volatile int lockState;
	static final int WAITER = 2;
	volatile Thread waiter;

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

    class TreeNode&lt;K, V&gt; extends Node&lt;K, V&gt; {
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

    class Node&lt;K, V&gt; implements Entry&lt;K, V&gt; {
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

}

