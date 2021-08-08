import java.util.concurrent.locks.ReentrantLock;

class ArrayBlockingQueue&lt;E&gt; extends AbstractQueue&lt;E&gt; implements BlockingQueue&lt;E&gt;, Serializable {
    /**
     * Returns the number of elements in this queue.
     *
     * @return the number of elements in this queue
     */
    public int size() {
	final ReentrantLock lock = this.lock;
	lock.lock();
	try {
	    return count;
	} finally {
	    lock.unlock();
	}
    }

    /** Main lock guarding all access */
    final ReentrantLock lock;
    /** Number of elements in the queue */
    int count;

}

