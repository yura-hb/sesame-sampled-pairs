import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import java.util.concurrent.atomic.AtomicBoolean;

class AsyncDataSetIterator implements DataSetIterator {
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
	this.thread.shutdown();
	buffer.clear();

	backedIterator.reset();
	shouldWork.set(true);
	this.thread = new AsyncPrefetchThread(buffer, backedIterator, terminator, null);

	/**
	 * We want to ensure, that background thread will have the same thread-&gt;device affinity, as master thread
	 */
	Nd4j.getAffinityManager().attachThreadToDevice(thread, deviceId);

	thread.setDaemon(true);
	thread.start();
	hasDepleted.set(false);

	nextElement = null;
    }

    protected BlockingQueue&lt;DataSet&gt; buffer;
    protected AsyncPrefetchThread thread;
    protected DataSetIterator backedIterator;
    protected AtomicBoolean shouldWork = new AtomicBoolean(true);
    protected DataSet terminator = new DataSet();
    protected Integer deviceId;
    protected AtomicBoolean hasDepleted = new AtomicBoolean(false);
    protected DataSet nextElement = null;

    class AsyncPrefetchThread extends Thread implements Runnable {
	protected BlockingQueue&lt;DataSet&gt; buffer;
	protected AsyncPrefetchThread thread;
	protected DataSetIterator backedIterator;
	protected AtomicBoolean shouldWork = new AtomicBoolean(true);
	protected DataSet terminator = new DataSet();
	protected Integer deviceId;
	protected AtomicBoolean hasDepleted = new AtomicBoolean(false);
	protected DataSet nextElement = null;

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
		log.debug("Manually destroying ADSI workspace");
		workspace.destroyWorkspace(true);
	    }
	}

	protected AsyncPrefetchThread(@NonNull BlockingQueue&lt;DataSet&gt; queue, @NonNull DataSetIterator iterator,
		@NonNull DataSet terminator, MemoryWorkspace workspace) {
	    this.queue = queue;
	    this.iterator = iterator;
	    this.terminator = terminator;

	    this.setDaemon(true);
	    this.setName("ADSI prefetch thread");
	}

    }

}

