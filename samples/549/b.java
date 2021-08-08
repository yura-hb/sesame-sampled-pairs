class ReentrantReadWriteLock implements ReadWriteLock, Serializable {
    /**
     * Queries whether any threads are waiting on the given condition
     * associated with the write lock. Note that because timeouts and
     * interrupts may occur at any time, a {@code true} return does
     * not guarantee that a future {@code signal} will awaken any
     * threads.  This method is designed primarily for use in
     * monitoring of the system state.
     *
     * @param condition the condition
     * @return {@code true} if there are any waiting threads
     * @throws IllegalMonitorStateException if this lock is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this lock
     * @throws NullPointerException if the condition is null
     */
    public boolean hasWaiters(Condition condition) {
	if (condition == null)
	    throw new NullPointerException();
	if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
	    throw new IllegalArgumentException("not owner");
	return sync.hasWaiters((AbstractQueuedSynchronizer.ConditionObject) condition);
    }

    /** Performs all synchronization mechanics */
    final Sync sync;

}

