import java.lang.invoke.VarHandle;

class ConcurrentLinkedQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements Queue&lt;E&gt;, Serializable {
    /**
     * Returns the number of elements in this queue.  If this queue
     * contains more than {@code Integer.MAX_VALUE} elements, returns
     * {@code Integer.MAX_VALUE}.
     *
     * &lt;p&gt;Beware that, unlike in most collections, this method is
     * &lt;em&gt;NOT&lt;/em&gt; a constant-time operation. Because of the
     * asynchronous nature of these queues, determining the current
     * number of elements requires an O(n) traversal.
     * Additionally, if elements are added or removed during execution
     * of this method, the returned result may be inaccurate.  Thus,
     * this method is typically not very useful in concurrent
     * applications.
     *
     * @return the number of elements in this queue
     */
    public int size() {
	restartFromHead: for (;;) {
	    int count = 0;
	    for (Node&lt;E&gt; p = first(); p != null;) {
		if (p.item != null)
		    if (++count == Integer.MAX_VALUE)
			break; // @see Collection.size()
		if (p == (p = p.next))
		    continue restartFromHead;
	    }
	    return count;
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
    private static final VarHandle HEAD;
    static final VarHandle NEXT;

    /**
     * Returns the first live (non-deleted) node on list, or null if none.
     * This is yet another variant of poll/peek; here returning the
     * first node, not element.  We could make peek() a wrapper around
     * first(), but that would cost an extra volatile read of item,
     * and the need to add a retry loop to deal with the possibility
     * of losing a race to a concurrent poll().
     */
    Node&lt;E&gt; first() {
	restartFromHead: for (;;) {
	    for (Node&lt;E&gt; h = head, p = h, q;; p = q) {
		boolean hasItem = (p.item != null);
		if (hasItem || (q = p.next) == null) {
		    updateHead(h, p);
		    return hasItem ? p : null;
		} else if (p == q)
		    continue restartFromHead;
	    }
	}
    }

    /**
     * Tries to CAS head to p. If successful, repoint old head to itself
     * as sentinel for succ(), below.
     */
    final void updateHead(Node&lt;E&gt; h, Node&lt;E&gt; p) {
	// assert h != null && p != null && (h == p || h.item == null);
	if (h != p && HEAD.compareAndSet(this, h, p))
	    NEXT.setRelease(h, h);
    }

}

