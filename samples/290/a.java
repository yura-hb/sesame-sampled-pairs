class MonitorBasedArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
    * Atomically removes all of the elements from this queue. The queue will be empty after this call
    * returns.
    */
    @Override
    public void clear() {
	final E[] items = this.items;
	final Monitor monitor = this.monitor;
	monitor.enter();
	try {
	    int i = takeIndex;
	    int k = count;
	    while (k-- &gt; 0) {
		items[i] = null;
		i = inc(i);
	    }
	    count = 0;
	    putIndex = 0;
	    takeIndex = 0;
	} finally {
	    monitor.leave();
	}
    }

    /** The queued items */
    final E[] items;
    /** Monitor guarding all access */
    final Monitor monitor;
    /** items index for next take, poll or remove */
    int takeIndex;
    /** Number of items in the queue */
    private int count;
    /** items index for next put, offer, or add. */
    int putIndex;

    /** Circularly increment i. */
    final int inc(int i) {
	return (++i == items.length) ? 0 : i;
    }

}

