class LinkedList&lt;E&gt; extends AbstractSequentialList&lt;E&gt; implements List&lt;E&gt;, Deque&lt;E&gt;, Cloneable, Serializable {
    /**
     * Removes all of the elements from this list.
     * The list will be empty after this call returns.
     */
    public void clear() {
	// Clearing all of the links between nodes is "unnecessary", but:
	// - helps a generational GC if the discarded nodes inhabit
	//   more than one generation
	// - is sure to free memory even if there is a reachable Iterator
	for (Node&lt;E&gt; x = first; x != null;) {
	    Node&lt;E&gt; next = x.next;
	    x.item = null;
	    x.next = null;
	    x.prev = null;
	    x = next;
	}
	first = last = null;
	size = 0;
	modCount++;
    }

    /**
     * Pointer to first node.
     */
    transient Node&lt;E&gt; first;
    /**
     * Pointer to last node.
     */
    transient Node&lt;E&gt; last;
    transient int size = 0;

}

