class ThreadFactoryBuilder {
    /**
    * Sets the priority for new threads created with this ThreadFactory.
    *
    * @param priority the priority for new Threads created with this ThreadFactory
    * @return this for the builder pattern
    */
    public ThreadFactoryBuilder setPriority(int priority) {
	// Thread#setPriority() already checks for validity. These error messages
	// are nicer though and will fail-fast.
	checkArgument(priority &gt;= Thread.MIN_PRIORITY, "Thread priority (%s) must be &gt;= %s", priority,
		Thread.MIN_PRIORITY);
	checkArgument(priority &lt;= Thread.MAX_PRIORITY, "Thread priority (%s) must be &lt;= %s", priority,
		Thread.MAX_PRIORITY);
	this.priority = priority;
	return this;
    }

    private Integer priority = null;

}

