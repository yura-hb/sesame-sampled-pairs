class BasicThreadFactory implements ThreadFactory {
    class Builder implements Builder&lt;BasicThreadFactory&gt; {
	/**
	 * Sets the priority for the threads created by the new {@code
	 * BasicThreadFactory}.
	 *
	 * @param priority the priority
	 * @return a reference to this {@code Builder}
	 */
	public Builder priority(final int priority) {
	    this.priority = Integer.valueOf(priority);
	    return this;
	}

	/** The priority. */
	private Integer priority;

    }

}

