class MonitorBasedArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
    * Removes a single instance of the specified element from this queue, if it is present. More
    * formally, removes an element &lt;tt&gt;e&lt;/tt&gt; such that &lt;tt&gt;o.equals(e)&lt;/tt&gt;, if this queue contains
    * one or more such elements. Returns &lt;tt&gt;true&lt;/tt&gt; if this queue contained the specified element
    * (or equivalently, if this queue changed as a result of the call).
    *
    * @param o element to be removed from this queue, if present
    * @return &lt;tt&gt;true&lt;/tt&gt; if this queue changed as a result of the call
    */
    @Override
    public boolean remove(@Nullable Object o) {
	if (o == null)
	    return false;
	final E[] items = this.items;
	final Monitor monitor = this.monitor;
	monitor.enter();
	try {
	    int i = takeIndex;
	    int k = 0;
	    for (;;) {
		if (k++ &gt;= count)
		    return false;
		if (o.equals(items[i])) {
		    removeAt(i);
		    return true;
		}
		i = inc(i);
	    }
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

    /** Circularly increment i. */
    final int inc(int i) {
	return (++i == items.length) ? 0 : i;
    }

}

