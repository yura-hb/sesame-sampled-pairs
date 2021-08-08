import java.util.concurrent.locks.ReentrantLock;

class Monitor {
    /**
    * Returns an estimate of the number of threads waiting for the given guard to become satisfied.
    * Note that because timeouts and interrupts may occur at any time, the estimate serves only as an
    * upper bound on the actual number of waiters. This method is designed for use in monitoring of
    * the system state, not for synchronization control.
    */
    public int getWaitQueueLength(Guard guard) {
	if (guard.monitor != this) {
	    throw new IllegalMonitorStateException();
	}
	lock.lock();
	try {
	    return guard.waiterCount;
	} finally {
	    lock.unlock();
	}
    }

    /** The lock underlying this monitor. */
    private final ReentrantLock lock;

}

