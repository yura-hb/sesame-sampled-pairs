class MonitorBasedArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
    * Returns the number of additional elements that this queue can ideally (in the absence of memory
    * or resource constraints) accept without blocking. This is always equal to the initial capacity
    * of this queue less the current &lt;tt&gt;size&lt;/tt&gt; of this queue.
    *
    * &lt;p&gt;Note that you &lt;em&gt;cannot&lt;/em&gt; always tell if an attempt to insert an element will succeed by
    * inspecting &lt;tt&gt;remainingCapacity&lt;/tt&gt; because it may be the case that another thread is about
    * to insert or remove an element.
    */
    @Override
    public int remainingCapacity() {
	final Monitor monitor = this.monitor;
	monitor.enter();
	try {
	    return items.length - count;
	} finally {
	    monitor.leave();
	}
    }

    /** Monitor guarding all access */
    final Monitor monitor;
    /** The queued items */
    final E[] items;
    /** Number of items in the queue */
    private int count;

}

