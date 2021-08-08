import java.lang.invoke.VarHandle;
import java.util.concurrent.locks.LockSupport;

class LinkedTransferQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements TransferQueue&lt;E&gt;, Serializable {
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
	    for (Node p = head, pred = null; p != null;) {
		Node q = p.next;
		final Object item;
		if ((item = p.item) != null) {
		    if (p.isData) {
			if (o.equals(item) && p.tryMatch(item, null)) {
			    skipDeadNodes(pred, p, p, q);
			    return true;
			}
			pred = p;
			p = q;
			continue;
		    }
		} else if (!p.isData)
		    break;
		for (Node c = p;; q = p.next) {
		    if (q == null || !q.isMatched()) {
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
     * A node from which the first live (non-matched) node (if any)
     * can be reached in O(1) time.
     * Invariants:
     * - all live nodes are reachable from head via .next
     * - head != null
     * - (tmp = head).next != tmp || tmp != head
     * Non-invariants:
     * - head may or may not be live
     * - it is permitted for tail to lag behind head, that is, for tail
     *   to not be reachable from head!
     */
    transient volatile Node head;
    static final VarHandle ITEM;
    static final VarHandle NEXT;
    private static final VarHandle HEAD;

    /**
     * Collapses dead (matched) nodes between pred and q.
     * @param pred the last known live node, or null if none
     * @param c the first dead node
     * @param p the last dead node
     * @param q p.next: the next live node, or null if at end
     * @return pred if pred still alive and CAS succeeded; else p
     */
    private Node skipDeadNodes(Node pred, Node c, Node p, Node q) {
	// assert pred != c;
	// assert p != q;
	// assert c.isMatched();
	// assert p.isMatched();
	if (q == null) {
	    // Never unlink trailing node.
	    if (c == p)
		return pred;
	    q = p;
	}
	return (tryCasSuccessor(pred, c, q) && (pred == null || !pred.isMatched())) ? pred : p;
    }

    /**
     * Tries to CAS pred.next (or head, if pred is null) from c to p.
     * Caller must ensure that we're not unlinking the trailing node.
     */
    private boolean tryCasSuccessor(Node pred, Node c, Node p) {
	// assert p != null;
	// assert c.isData != (c.item != null);
	// assert c != p;
	if (pred != null)
	    return pred.casNext(c, p);
	if (casHead(c, p)) {
	    c.selfLink();
	    return true;
	}
	return false;
    }

    private boolean casHead(Node cmp, Node val) {
	return HEAD.compareAndSet(this, cmp, val);
    }

    class Node {
	/**
	* A node from which the first live (non-matched) node (if any)
	* can be reached in O(1) time.
	* Invariants:
	* - all live nodes are reachable from head via .next
	* - head != null
	* - (tmp = head).next != tmp || tmp != head
	* Non-invariants:
	* - head may or may not be live
	* - it is permitted for tail to lag behind head, that is, for tail
	*   to not be reachable from head!
	*/
	transient volatile Node head;
	static final VarHandle ITEM;
	static final VarHandle NEXT;
	private static final VarHandle HEAD;

	/** Tries to CAS-match this node; if successful, wakes waiter. */
	final boolean tryMatch(Object cmp, Object val) {
	    if (casItem(cmp, val)) {
		LockSupport.unpark(waiter);
		return true;
	    }
	    return false;
	}

	/**
	 * Returns true if this node has been matched, including the
	 * case of artificial matches due to cancellation.
	 */
	final boolean isMatched() {
	    return isData == (item == null);
	}

	final boolean casItem(Object cmp, Object val) {
	    // assert isData == (cmp != null);
	    // assert isData == (val == null);
	    // assert !(cmp instanceof Node);
	    return ITEM.compareAndSet(this, cmp, val);
	}

	final boolean casNext(Node cmp, Node val) {
	    // assert val != null;
	    return NEXT.compareAndSet(this, cmp, val);
	}

	/**
	 * Links node to itself to avoid garbage retention.  Called
	 * only after CASing head field, so uses relaxed write.
	 */
	final void selfLink() {
	    // assert isMatched();
	    NEXT.setRelease(this, this);
	}

    }

}

