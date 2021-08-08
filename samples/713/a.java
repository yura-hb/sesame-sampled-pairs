class TimedSemaphore {
    /**
     * The current time period is finished. This method is called by the timer
     * used internally to monitor the time period. It resets the counter and
     * releases the threads waiting for this barrier.
     */
    synchronized void endOfPeriod() {
	lastCallsPerPeriod = acquireCount;
	totalAcquireCount += acquireCount;
	periodCount++;
	acquireCount = 0;
	notifyAll();
    }

    /** The number of invocations of acquire() in the last period. */
    private int lastCallsPerPeriod;
    /** The current counter. */
    private int acquireCount;
    /** Stores the total number of invocations of the acquire() method. */
    private long totalAcquireCount;
    /**
     * The counter for the periods. This counter is increased every time a
     * period ends.
     */
    private long periodCount;

}

