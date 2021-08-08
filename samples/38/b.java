class ReentrantLock implements Lock, Serializable {
    /**
     * Returns an estimate of the number of threads waiting on the
     * given condition associated with this lock. Note that because
     * timeouts and interrupts may occur at any time, the estimate
     * serves only as an upper bound on the actual number of waiters.
     * This method is designed for use in monitoring of the system
     * state, not for synchronization control.
     *
     * @param condition the condition
     * @return the estimated number of waiting threads
     * @throws IllegalMonitorStateException if this lock is not held
     * @throws IllegalArgumentException if the given condition is
     *         not associated with this lock
     * @throws NullPointerException if the condition is null
     */
    public int getWaitQueueLength(Condition condition) {
	if (condition == null)
	    throw new NullPointerException();
	if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
	    throw new IllegalArgumentException("not owner");
	return sync.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject) condition);
    }

    /** Synchronizer providing all implementation mechanics */
    private final Sync sync;

}

