import java.util.PriorityQueue;

class MonitorBasedPriorityBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
    * Removes a single instance of the specified element from this queue, if it is present. More
    * formally, removes an element {@code e} such that {@code o.equals(e)}, if this queue contains
    * one or more such elements. Returns {@code true} if and only if this queue contained the
    * specified element (or equivalently, if this queue changed as a result of the call).
    *
    * @param o element to be removed from this queue, if present
    * @return &lt;tt&gt;true&lt;/tt&gt; if this queue changed as a result of the call
    */
    @Override
    public boolean remove(@Nullable Object o) {
	final Monitor monitor = this.monitor;
	monitor.enter();
	try {
	    return q.remove(o);
	} finally {
	    monitor.leave();
	}
    }

    final Monitor monitor = new Monitor(true);
    final PriorityQueue&lt;E&gt; q;

}

