class StopWatch {
    /**
     * &lt;p&gt;
     * Get the time on the stopwatch in nanoseconds.
     * &lt;/p&gt;
     *
     * &lt;p&gt;
     * This is either the time between the start and the moment this method is called, or the amount of time between
     * start and stop.
     * &lt;/p&gt;
     *
     * @return the time in nanoseconds
     * @since 3.0
     */
    public long getNanoTime() {
	if (this.runningState == State.STOPPED || this.runningState == State.SUSPENDED) {
	    return this.stopTime - this.startTime;
	} else if (this.runningState == State.UNSTARTED) {
	    return 0;
	} else if (this.runningState == State.RUNNING) {
	    return System.nanoTime() - this.startTime;
	}
	throw new RuntimeException("Illegal running state has occurred.");
    }

    /**
     * The current running state of the StopWatch.
     */
    private State runningState = State.UNSTARTED;
    /**
     * The stop time.
     */
    private long stopTime;
    /**
     * The start time.
     */
    private long startTime;

}

