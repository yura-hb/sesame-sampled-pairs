class StopWatch {
    /**
     * &lt;p&gt;
     * Start the stopwatch.
     * &lt;/p&gt;
     *
     * &lt;p&gt;
     * This method starts a new timing session, clearing any previous values.
     * &lt;/p&gt;
     *
     * @throws IllegalStateException
     *             if the StopWatch is already running.
     */
    public void start() {
	if (this.runningState == State.STOPPED) {
	    throw new IllegalStateException("Stopwatch must be reset before being restarted. ");
	}
	if (this.runningState != State.UNSTARTED) {
	    throw new IllegalStateException("Stopwatch already started. ");
	}
	this.startTime = System.nanoTime();
	this.startTimeMillis = System.currentTimeMillis();
	this.runningState = State.RUNNING;
    }

    /**
     * The current running state of the StopWatch.
     */
    private State runningState = State.UNSTARTED;
    /**
     * The start time.
     */
    private long startTime;
    /**
     * The start time in Millis - nanoTime is only for elapsed time so we
     * need to also store the currentTimeMillis to maintain the old
     * getStartTime API.
     */
    private long startTimeMillis;

}

