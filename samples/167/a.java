class TimerWheel&lt;K, V&gt; {
    /**
    * Schedules a timer event for the node.
    *
    * @param node the entry in the cache
    */
    public void schedule(@NonNull Node&lt;K, V&gt; node) {
	Node&lt;K, V&gt; sentinel = findBucket(node.getVariableTime());
	link(sentinel, node);
    }

    long nanos;
    final Node&lt;K, V&gt;[][] wheel;
    static final long[] SPANS = { ceilingPowerOfTwo(TimeUnit.SECONDS.toNanos(1)), // 1.07s
	    ceilingPowerOfTwo(TimeUnit.MINUTES.toNanos(1)), // 1.14m
	    ceilingPowerOfTwo(TimeUnit.HOURS.toNanos(1)), // 1.22h
	    ceilingPowerOfTwo(TimeUnit.DAYS.toNanos(1)), // 1.63d
	    BUCKETS[3] * ceilingPowerOfTwo(TimeUnit.DAYS.toNanos(1)), // 6.5d
	    BUCKETS[3] * ceilingPowerOfTwo(TimeUnit.DAYS.toNanos(1)), // 6.5d
    };
    static final long[] SHIFT = { Long.SIZE - Long.numberOfLeadingZeros(SPANS[0] - 1),
	    Long.SIZE - Long.numberOfLeadingZeros(SPANS[1] - 1), Long.SIZE - Long.numberOfLeadingZeros(SPANS[2] - 1),
	    Long.SIZE - Long.numberOfLeadingZeros(SPANS[3] - 1), Long.SIZE - Long.numberOfLeadingZeros(SPANS[4] - 1), };

    /**
    * Determines the bucket that the timer event should be added to.
    *
    * @param time the time when the event fires
    * @return the sentinel at the head of the bucket
    */
    Node&lt;K, V&gt; findBucket(long time) {
	long duration = time - nanos;
	int length = wheel.length - 1;
	for (int i = 0; i &lt; length; i++) {
	    if (duration &lt; SPANS[i + 1]) {
		int ticks = (int) (time &gt;&gt; SHIFT[i]);
		int index = ticks & (wheel[i].length - 1);
		return wheel[i][index];
	    }
	}
	return wheel[length][0];
    }

    /** Adds the entry at the tail of the bucket's list. */
    void link(Node&lt;K, V&gt; sentinel, Node&lt;K, V&gt; node) {
	node.setPreviousInVariableOrder(sentinel.getPreviousInVariableOrder());
	node.setNextInVariableOrder(sentinel);

	sentinel.getPreviousInVariableOrder().setNextInVariableOrder(node);
	sentinel.setPreviousInVariableOrder(node);
    }

}

