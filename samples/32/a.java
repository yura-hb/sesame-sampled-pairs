import java.util.PriorityQueue;

class MonitorBasedPriorityBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
    * Atomically removes all of the elements from this queue. The queue will be empty after this call
    * returns.
    */
    @Override
    public void clear() {
	final Monitor monitor = this.monitor;
	monitor.enter();
	try {
	    q.clear();
	} finally {
	    monitor.leave();
	}
    }

    final Monitor monitor = new Monitor(true);
    final PriorityQueue&lt;E&gt; q;

}

