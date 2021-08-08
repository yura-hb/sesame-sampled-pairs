import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.LongAdder;

class ConcurrentSkipListMap&lt;K, V&gt; extends AbstractMap&lt;K, V&gt;
	implements ConcurrentNavigableMap&lt;K, V&gt;, Cloneable, Serializable {
    /**
     * Removes all of the mappings from this map.
     */
    public void clear() {
	Index&lt;K, V&gt; h, r, d;
	Node&lt;K, V&gt; b;
	VarHandle.acquireFence();
	while ((h = head) != null) {
	    if ((r = h.right) != null) // remove indices
		RIGHT.compareAndSet(h, r, null);
	    else if ((d = h.down) != null) // remove levels
		HEAD.compareAndSet(this, h, d);
	    else {
		long count = 0L;
		if ((b = h.node) != null) { // remove nodes
		    Node&lt;K, V&gt; n;
		    V v;
		    while ((n = b.next) != null) {
			if ((v = n.val) != null && VAL.compareAndSet(n, v, null)) {
			    --count;
			    v = null;
			}
			if (v == null)
			    unlinkNode(b, n);
		    }
		}
		if (count != 0L)
		    addCount(count);
		else
		    break;
	    }
	}
    }

    /** Lazily initialized topmost index of the skiplist. */
    private transient Index&lt;K, V&gt; head;
    private static final VarHandle RIGHT;
    private static final VarHandle HEAD;
    private static final VarHandle VAL;
    private static final VarHandle NEXT;
    /** Lazily initialized element count */
    private transient LongAdder adder;
    private static final VarHandle ADDER;

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
	/** Lazily initialized topmost index of the skiplist. */
	private transient Index&lt;K, V&gt; head;
	private static final VarHandle RIGHT;
	private static final VarHandle HEAD;
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

}

