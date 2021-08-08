class MonitorBasedArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
    * Returns the number of elements in this queue.
    *
    * @return the number of elements in this queue
    */
    @Override
    public int size() {
	final Monitor monitor = this.monitor;
	monitor.enter();
	try {
	    return count;
	} finally {
	    monitor.leave();
	}
    }

    /** Monitor guarding all access */
    final Monitor monitor;
    /** Number of items in the queue */
    private int count;

}

