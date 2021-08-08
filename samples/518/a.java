class MonitorBasedArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
    * Utility for remove and iterator.remove: Delete item at position i. Call only when occupying
    * monitor.
    */
    void removeAt(int i) {
	final E[] items = this.items;
	// if removing front item, just advance
	if (i == takeIndex) {
	    items[takeIndex] = null;
	    takeIndex = inc(takeIndex);
	} else {
	    // slide over all others up through putIndex.
	    for (;;) {
		int nexti = inc(i);
		if (nexti != putIndex) {
		    items[i] = items[nexti];
		    i = nexti;
		} else {
		    items[i] = null;
		    putIndex = i;
		    break;
		}
	    }
	}
	--count;
    }

    /** The queued items */
    final E[] items;
    /** items index for next take, poll or remove */
    int takeIndex;
    /** items index for next put, offer, or add. */
    int putIndex;
    /** Number of items in the queue */
    private int count;

    /** Circularly increment i. */
    final int inc(int i) {
	return (++i == items.length) ? 0 : i;
    }

}

