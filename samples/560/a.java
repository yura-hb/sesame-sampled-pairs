import java.util.PriorityQueue;

class MonitorBasedPriorityBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
    * Returns an array containing all of the elements in this queue. The returned array elements are
    * in no particular order.
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
	final Monitor monitor = this.monitor;
	monitor.enter();
	try {
	    return q.toArray();
	} finally {
	    monitor.leave();
	}
    }

    final Monitor monitor = new Monitor(true);
    final PriorityQueue&lt;E&gt; q;

}

