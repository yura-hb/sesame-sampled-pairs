import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import java.util.concurrent.atomic.AtomicBoolean;

class AsyncMultiDataSetIterator implements MultiDataSetIterator {
    /**
     * Resets the iterator back to the beginning
     */
    @Override
    public void reset() {
	buffer.clear();

	if (thread != null)
	    thread.interrupt();
	try {
	    // Shutdown() should be a synchronous operation since the iterator is reset after shutdown() is
	    // called in AsyncLabelAwareIterator.reset().
	    if (thread != null)
		thread.join();
	} catch (InterruptedException e) {
	    Thread.currentThread().interrupt();
	    throw new RuntimeException(e);
	}
	thread.shutdown();
	buffer.clear();

	backedIterator.reset();
	shouldWork.set(true);
	this.thread = new AsyncPrefetchThread(buffer, backedIterator, terminator);

	/**
	 * We want to ensure, that background thread will have the same thread-&gt;device affinity, as master thread
	 */
	Nd4j.getAffinityManager().attachThreadToDevice(thread, deviceId);

	thread.setDaemon(true);
	thread.start();

	hasDepleted.set(false);

	nextElement = null;
    }

    protected BlockingQueue&lt;MultiDataSet&gt; buffer;
    protected AsyncPrefetchThread thread;
    protected MultiDataSetIterator backedIterator;
    protected AtomicBoolean shouldWork = new AtomicBoolean(true);
    protected MultiDataSet terminator = new org.nd4j.linalg.dataset.MultiDataSet();
    protected Integer deviceId;
    protected AtomicBoolean hasDepleted = new AtomicBoolean(false);
    protected MultiDataSet nextElement = null;

    class AsyncPrefetchThread extends Thread implements Runnable {
	protected BlockingQueue&lt;MultiDataSet&gt; buffer;
	protected AsyncPrefetchThread thread;
	protected MultiDataSetIterator backedIterator;
	protected AtomicBoolean shouldWork = new AtomicBoolean(true);
	protected MultiDataSet terminator = new org.nd4j.linalg.dataset.MultiDataSet();
	protected Integer deviceId;
	protected AtomicBoolean hasDepleted = new AtomicBoolean(false);
	protected MultiDataSet nextElement = null;

	public void shutdown() {
	    synchronized (this) {
		while (!isShutdown) {
		    try {
			this.wait();
		    } catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		    }
		}
	    }

	    if (workspace != null) {
		log.debug("Manually destroying AMDSI workspace");
		workspace.destroyWorkspace(true);
		workspace = null;
	    }
	}

	protected AsyncPrefetchThread(@NonNull BlockingQueue&lt;MultiDataSet&gt; queue,
		@NonNull MultiDataSetIterator iterator, @NonNull MultiDataSet terminator) {
	    this.queue = queue;
	    this.iterator = iterator;
	    this.terminator = terminator;

	    this.setDaemon(true);
	    this.setName("AMDSI prefetch thread");
	}

    }

}

