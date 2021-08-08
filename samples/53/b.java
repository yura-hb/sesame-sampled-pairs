import java.lang.invoke.VarHandle;

class ConcurrentLinkedQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements Queue&lt;E&gt;, Serializable {
    /**
     * Removes a single instance of the specified element from this queue,
     * if it is present.  More formally, removes an element {@code e} such
     * that {@code o.equals(e)}, if this queue contains one or more such
     * elements.
     * Returns {@code true} if this queue contained the specified element
     * (or equivalently, if this queue changed as a result of the call).
     *
     * @param o element to be removed from this queue, if present
     * @return {@code true} if this queue changed as a result of the call
     */
    public boolean remove(Object o) {
	if (o == null)
	    return false;
	restartFromHead: for (;;) {
	    for (Node&lt;E&gt; p = head, pred = null; p != null;) {
		Node&lt;E&gt; q = p.next;
		final E item;
		if ((item = p.item) != null) {
		    if (o.equals(item) && p.casItem(item, null)) {
			skipDeadNodes(pred, p, p, q);
			return true;
		    }
		    pred = p;
		    p = q;
		    continue;
		}
		for (Node&lt;E&gt; c = p;; q = p.next) {
		    if (q == null || q.item != null) {
			pred = skipDeadNodes(pred, c, p, q);
			p = q;
			break;
		    }
		    if (p == (p = q))
			continue restartFromHead;
		}
	    }
	    return false;
	}
    }

    /**
     * A node from which the first live (non-deleted) node (if any)
     * can be reached in O(1) time.
     * Invariants:
     * - all live nodes are reachable from head via succ()
     * - head != null
     * - (tmp = head).next != tmp || tmp != head
     * Non-invariants:
     * - head.item may or may not be null.
     * - it is permitted for tail to lag behind head, that is, for tail
     *   to not be reachable from head!
     */
    transient volatile Node&lt;E&gt; head;
    static final VarHandle ITEM;
    static final VarHandle NEXT;
    private static final VarHandle HEAD;

    /**
     * Collapse dead nodes between pred and q.
     * @param pred the last known live node, or null if none
     * @param c the first dead node
     * @param p the last dead node
     * @param q p.next: the next live node, or null if at end
     * @return either old pred or p if pred dead or CAS failed
     */
    private Node&lt;E&gt; skipDeadNodes(Node&lt;E&gt; pred, Node&lt;E&gt; c, Node&lt;E&gt; p, Node&lt;E&gt; q) {
	// assert pred != c;
	// assert p != q;
	// assert c.item == null;
	// assert p.item == null;
	if (q == null) {
	    // Never unlink trailing node.
	    if (c == p)
		return pred;
	    q = p;
	}
	return (tryCasSuccessor(pred, c, q) && (pred == null || ITEM.get(pred) != null)) ? pred : p;
    }

    /**
     * Tries to CAS pred.next (or head, if pred is null) from c to p.
     * Caller must ensure that we're not unlinking the trailing node.
     */
    private boolean tryCasSuccessor(Node&lt;E&gt; pred, Node&lt;E&gt; c, Node&lt;E&gt; p) {
	// assert p != null;
	// assert c.item == null;
	// assert c != p;
	if (pred != null)
	    return NEXT.compareAndSet(pred, c, p);
	if (HEAD.compareAndSet(this, c, p)) {
	    NEXT.setRelease(c, c);
	    return true;
	}
	return false;
    }

    class Node&lt;E&gt; {
	/**
	* A node from which the first live (non-deleted) node (if any)
	* can be reached in O(1) time.
	* Invariants:
	* - all live nodes are reachable from head via succ()
	* - head != null
	* - (tmp = head).next != tmp || tmp != head
	* Non-invariants:
	* - head.item may or may not be null.
	* - it is permitted for tail to lag behind head, that is, for tail
	*   to not be reachable from head!
	*/
	transient volatile Node&lt;E&gt; head;
	static final VarHandle ITEM;
	static final VarHandle NEXT;
	private static final VarHandle HEAD;

	boolean casItem(E cmp, E val) {
	    // assert item == cmp || item == null;
	    // assert cmp != null;
	    // assert val == null;
	    return ITEM.compareAndSet(this, cmp, val);
	}

    }

}

