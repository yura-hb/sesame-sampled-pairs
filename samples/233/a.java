class StopWatch {
    /**
     * &lt;p&gt;
     * Remove a split.
     * &lt;/p&gt;
     *
     * &lt;p&gt;
     * This method clears the stop time. The start time is unaffected, enabling timing from the original start point to
     * continue.
     * &lt;/p&gt;
     *
     * @throws IllegalStateException
     *             if the StopWatch has not been split.
     */
    public void unsplit() {
	if (this.splitState != SplitState.SPLIT) {
	    throw new IllegalStateException("Stopwatch has not been split. ");
	}
	this.splitState = SplitState.UNSPLIT;
    }

    /**
     * Whether the stopwatch has a split time recorded.
     */
    private SplitState splitState = SplitState.UNSPLIT;

}

