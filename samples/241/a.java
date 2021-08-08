class ArcPolicy implements Policy {
    class Node {
	/** Removes the node from the list. */
	public void remove() {
	    checkState(key != Long.MIN_VALUE);
	    prev.next = next;
	    next.prev = prev;
	    prev = next = null;
	    type = null;
	}

	final long key;
	Node prev;
	Node next;
	QueueType type;

    }

}

