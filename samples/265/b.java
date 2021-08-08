class MonitorBasedArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
    * Returns an iterator over the elements in this queue in proper sequence. The returned
    * &lt;tt&gt;Iterator&lt;/tt&gt; is a "weakly consistent" iterator that will never throw {@link
    * ConcurrentModificationException}, and guarantees to traverse elements as they existed upon
    * construction of the iterator, and may (but is not guaranteed to) reflect any modifications
    * subsequent to construction.
    *
    * @return an iterator over the elements in this queue in proper sequence
    */
    @Override
    public Iterator&lt;E&gt; iterator() {
	final Monitor monitor = this.monitor;
	monitor.enter();
	try {
	    return new Itr();
	} finally {
	    monitor.leave();
	}
    }

    /** Monitor guarding all access */
    final Monitor monitor;
    /** Number of items in the queue */
    private int count;
    /** items index for next take, poll or remove */
    int takeIndex;
    /** The queued items */
    final E[] items;

    class Itr implements Iterator&lt;E&gt; {
	/** Monitor guarding all access */
	final Monitor monitor;
	/** Number of items in the queue */
	private int count;
	/** items index for next take, poll or remove */
	int takeIndex;
	/** The queued items */
	final E[] items;

	Itr() {
	    lastRet = -1;
	    if (count == 0)
		nextIndex = -1;
	    else {
		nextIndex = takeIndex;
		nextItem = items[takeIndex];
	    }
	}

    }

}

