import java.util.PriorityQueue;

class MonitorBasedPriorityBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
    * Inserts the specified element into this priority queue.
    *
    * @param e the element to add
    * @return &lt;tt&gt;true&lt;/tt&gt; (as specified by {@link Queue#offer})
    * @throws ClassCastException if the specified element cannot be compared with elements currently
    *     in the priority queue according to the priority queue's ordering
    * @throws NullPointerException if the specified element is null
    */
    @Override
    public boolean offer(E e) {
	final Monitor monitor = this.monitor;
	monitor.enter();
	try {
	    boolean ok = q.offer(e);
	    if (!ok) {
		throw new AssertionError();
	    }
	    return true;
	} finally {
	    monitor.leave();
	}
    }

    final Monitor monitor = new Monitor(true);
    final PriorityQueue&lt;E&gt; q;

}

