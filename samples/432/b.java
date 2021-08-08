import jdk.internal.misc.Unsafe;

class ConcurrentHashMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt;, Serializable {
    /**
     * Returns {@code true} if this map maps one or more keys to the
     * specified value. Note: This method may require a full traversal
     * of the map, and is much slower than method {@code containsKey}.
     *
     * @param value value whose presence in this map is to be tested
     * @return {@code true} if this map maps one or more keys to the
     *         specified value
     * @throws NullPointerException if the specified value is null
     */
    public boolean containsValue(Object value) {
	if (value == null)
	    throw new NullPointerException();
	Node&lt;K, V&gt;[] t;
	if ((t = table) != null) {
	    Traverser&lt;K, V&gt; it = new Traverser&lt;K, V&gt;(t, t.length, 0, t.length);
	    for (Node&lt;K, V&gt; p; (p = it.advance()) != null;) {
		V v;
		if ((v = p.val) == value || (v != null && value.equals(v)))
		    return true;
	    }
	}
	return false;
    }

    /**
     * The array of bins. Lazily initialized upon first insertion.
     * Size is always a power of two. Accessed directly by iterators.
     */
    transient volatile Node&lt;K, V&gt;[] table;
    private static final Unsafe U = Unsafe.getUnsafe();
    private static final int ASHIFT;
    private static final int ABASE;

    @SuppressWarnings("unchecked")
    static final &lt;K, V&gt; Node&lt;K, V&gt; tabAt(Node&lt;K, V&gt;[] tab, int i) {
	return (Node&lt;K, V&gt;) U.getObjectAcquire(tab, ((long) i &lt;&lt; ASHIFT) + ABASE);
    }

    class Traverser&lt;K, V&gt; {
	/**
	* The array of bins. Lazily initialized upon first insertion.
	* Size is always a power of two. Accessed directly by iterators.
	*/
	transient volatile Node&lt;K, V&gt;[] table;
	private static final Unsafe U = Unsafe.getUnsafe();
	private static final int ASHIFT;
	private static final int ABASE;

	Traverser(Node&lt;K, V&gt;[] tab, int size, int index, int limit) {
	    this.tab = tab;
	    this.baseSize = size;
	    this.baseIndex = this.index = index;
	    this.baseLimit = limit;
	    this.next = null;
	}

	/**
	 * Advances if possible, returning next valid node, or null if none.
	 */
	final Node&lt;K, V&gt; advance() {
	    Node&lt;K, V&gt; e;
	    if ((e = next) != null)
		e = e.next;
	    for (;;) {
		Node&lt;K, V&gt;[] t;
		int i, n; // must use locals in checks
		if (e != null)
		    return next = e;
		if (baseIndex &gt;= baseLimit || (t = tab) == null || (n = t.length) &lt;= (i = index) || i &lt; 0)
		    return next = null;
		if ((e = tabAt(t, i)) != null && e.hash &lt; 0) {
		    if (e instanceof ForwardingNode) {
			tab = ((ForwardingNode&lt;K, V&gt;) e).nextTable;
			e = null;
			pushState(t, i, n);
			continue;
		    } else if (e instanceof TreeBin)
			e = ((TreeBin&lt;K, V&gt;) e).first;
		    else
			e = null;
		}
		if (stack != null)
		    recoverState(n);
		else if ((index = i + baseSize) &gt;= n)
		    index = ++baseIndex; // visit upper slots if present
	    }
	}

	/**
	 * Saves traversal state upon encountering a forwarding node.
	 */
	private void pushState(Node&lt;K, V&gt;[] t, int i, int n) {
	    TableStack&lt;K, V&gt; s = spare; // reuse if possible
	    if (s != null)
		spare = s.next;
	    else
		s = new TableStack&lt;K, V&gt;();
	    s.tab = t;
	    s.length = n;
	    s.index = i;
	    s.next = stack;
	    stack = s;
	}

	/**
	 * Possibly pops traversal state.
	 *
	 * @param n length of current table
	 */
	private void recoverState(int n) {
	    TableStack&lt;K, V&gt; s;
	    int len;
	    while ((s = stack) != null && (index += (len = s.length)) &gt;= n) {
		n = len;
		index = s.index;
		tab = s.tab;
		s.tab = null;
		TableStack&lt;K, V&gt; next = s.next;
		s.next = spare; // save for reuse
		stack = next;
		spare = s;
	    }
	    if (s == null && (index += baseSize) &gt;= n)
		index = ++baseIndex;
	}

    }

}

