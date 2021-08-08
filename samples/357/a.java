class MonitorBasedArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
    * Returns &lt;tt&gt;true&lt;/tt&gt; if this queue contains the specified element. More formally, returns
    * &lt;tt&gt;true&lt;/tt&gt; if and only if this queue contains at least one element &lt;tt&gt;e&lt;/tt&gt; such that
    * &lt;tt&gt;o.equals(e)&lt;/tt&gt;.
    *
    * @param o object to be checked for containment in this queue
    * @return &lt;tt&gt;true&lt;/tt&gt; if this queue contains the specified element
    */
    @Override
    public boolean contains(@Nullable Object o) {
	if (o == null)
	    return false;
	final E[] items = this.items;
	final Monitor monitor = this.monitor;
	monitor.enter();
	try {
	    int i = takeIndex;
	    int k = 0;
	    while (k++ &lt; count) {
		if (o.equals(items[i]))
		    return true;
		i = inc(i);
	    }
	    return false;
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

    /** Circularly increment i. */
    final int inc(int i) {
	return (++i == items.length) ? 0 : i;
    }

}

