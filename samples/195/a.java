class SharedTrainingMaster extends BaseTrainingMaster&lt;SharedTrainingResult, SharedTrainingWorker&gt;
	implements TrainingMaster&lt;SharedTrainingResult, SharedTrainingWorker&gt; {
    class Builder {
	/**
	 * This method allows you to artificially extend iteration time using Thread.sleep() for a given time.
	 *
	 * PLEASE NOTE: Never use that option in production environment. It's suited for debugging purposes only.
	 *
	 * @param timeMs
	 * @return
	 */
	@Deprecated
	public Builder debugLongerIterations(long timeMs) {
	    if (timeMs &lt; 0)
		timeMs = 0L;
	    this.debugLongerIterations = timeMs;
	    return this;
	}

	protected long debugLongerIterations = 0L;

    }

}

