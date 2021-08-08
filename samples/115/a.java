import java.util.concurrent.locks.ReentrantReadWriteLock;

class AtomicThrowable {
    /**
     * This method updates state only if it wasn't set before
     *
     * @param t
     */
    public void setIfFirst(Throwable t) {
	try {
	    lock.writeLock().lock();

	    if (this.t == null)
		this.t = t;
	} finally {
	    lock.writeLock().unlock();
	}
    }

    protected ReentrantReadWriteLock lock;
    protected volatile Throwable t = null;

}

