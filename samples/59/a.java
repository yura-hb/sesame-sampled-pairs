class MonitorBasedArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
    * Inserts the specified element at the tail of this queue if it is possible to do so immediately
    * without exceeding the queue's capacity, returning &lt;tt&gt;true&lt;/tt&gt; upon success and &lt;tt&gt;false&lt;/tt&gt;
    * if this queue is full. This method is generally preferable to method {@link #add}, which can
    * fail to insert an element only by throwing an exception.
    *
    * @throws NullPointerException if the specified element is null
    */
    @Override
    public boolean offer(E e) {
	if (e == null)
	    throw new NullPointerException();
	final Monitor monitor = this.monitor;
	if (monitor.enterIf(notFull)) {
	    try {
		insert(e);
		return true;
	    } finally {
		monitor.leave();
	    }
	} else {
	    return false;
	}
    }

    /** Monitor guarding all access */
    final Monitor monitor;
    /** Guard for waiting puts */
    private final Monitor.Guard notFull;
    /** The queued items */
    final E[] items;
    /** items index for next put, offer, or add. */
    int putIndex;
    /** Number of items in the queue */
    private int count;

    /**
    * Inserts element at current put position, advances, and signals. Call only when occupying
    * monitor.
    */
    private void insert(E x) {
	items[putIndex] = x;
	putIndex = inc(putIndex);
	++count;
    }

    /** Circularly increment i. */
    final int inc(int i) {
	return (++i == items.length) ? 0 : i;
    }

}

