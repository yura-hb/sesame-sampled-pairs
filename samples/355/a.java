class MonitorBasedArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
    * Returns an array containing all of the elements in this queue, in proper sequence.
    *
    * &lt;p&gt;The returned array will be "safe" in that no references to it are maintained by this queue.
    * (In other words, this method must allocate a new array). The caller is thus free to modify the
    * returned array.
    *
    * &lt;p&gt;This method acts as bridge between array-based and collection-based APIs.
    *
    * @return an array containing all of the elements in this queue
    */
    @Override
    public Object[] toArray() {
	final E[] items = this.items;
	final Monitor monitor = this.monitor;
	monitor.enter();
	try {
	    Object[] a = new Object[count];
	    int k = 0;
	    int i = takeIndex;
	    while (k &lt; count) {
		a[k++] = items[i];
		i = inc(i);
	    }
	    return a;
	} finally {
	    monitor.leave();
	}
    }

    /** The queued items */
    final E[] items;
    /** Monitor guarding all access */
    final Monitor monitor;
    /** Number of items in the queue */
    private int count;
    /** items index for next take, poll or remove */
    int takeIndex;

    /** Circularly increment i. */
    final int inc(int i) {
	return (++i == items.length) ? 0 : i;
    }

}

