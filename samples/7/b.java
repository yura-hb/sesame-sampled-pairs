import java.util.concurrent.atomic.AtomicInteger;

class LinkedBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt;, Serializable {
    /**
     * Returns the number of additional elements that this queue can ideally
     * (in the absence of memory or resource constraints) accept without
     * blocking. This is always equal to the initial capacity of this queue
     * less the current {@code size} of this queue.
     *
     * &lt;p&gt;Note that you &lt;em&gt;cannot&lt;/em&gt; always tell if an attempt to insert
     * an element will succeed by inspecting {@code remainingCapacity}
     * because it may be the case that another thread is about to
     * insert or remove an element.
     */
    public int remainingCapacity() {
	return capacity - count.get();
    }

    /** The capacity bound, or Integer.MAX_VALUE if none */
    private final int capacity;
    /** Current number of elements */
    private final AtomicInteger count = new AtomicInteger();

}

