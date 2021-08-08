import java.util.PriorityQueue;

class MonitorBasedPriorityBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt; {
    /**
    * Returns {@code true} if this queue contains the specified element. More formally, returns
    * {@code true} if and only if this queue contains at least one element {@code e} such that {@code
    * o.equals(e)}.
    *
    * @param o object to be checked for containment in this queue
    * @return &lt;tt&gt;true&lt;/tt&gt; if this queue contains the specified element
    */
    @Override
    public boolean contains(@Nullable Object o) {
	final Monitor monitor = this.monitor;
	monitor.enter();
	try {
	    return q.contains(o);
	} finally {
	    monitor.leave();
	}
    }

    final Monitor monitor = new Monitor(true);
    final PriorityQueue&lt;E&gt; q;

}

