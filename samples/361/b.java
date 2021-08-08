import java.nio.LongBuffer;

class PerfCounter {
    /**
     * Adds the given value to the perf counter.
     */
    public synchronized void add(long value) {
	long res = get() + value;
	lb.put(0, res);
    }

    private final LongBuffer lb;

    /**
     * Returns the current value of the perf counter.
     */
    public synchronized long get() {
	return lb.get(0);
    }

}

