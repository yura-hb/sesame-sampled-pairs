class MonitorBasedArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
    * Inserts the specified element at the tail of this queue, waiting up to the specified wait time
    * for space to become available if the queue is full.
    *
    * @throws InterruptedException {@inheritDoc}
    * @throws NullPointerException {@inheritDoc}
    */
    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {

	if (e == null)
	    throw new NullPointerException();
	final Monitor monitor = this.monitor;
	if (monitor.enterWhen(notFull, timeout, unit)) {
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

