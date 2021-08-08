import org.nd4j.parameterserver.distributed.v2.transport.Transport;
import java.util.Collection;
import java.util.List;

class ModelParameterServer {
    /**
     * This method stops parameter server
     */
    public synchronized void shutdown() {
	if (stopLock.get())
	    return;

	// shutting down underlying transport
	transport.shutdown();

	// disposing INDArray flow
	disposable.dispose();

	updaterParamsSubscribers.clear();
	modelParamsSubsribers.clear();
	updatesSubscribers.clear();
	updatesQueue.clear();

	// state that we're done
	launchLock.set(false);

	stopLock.set(true);
    }

    private final AtomicBoolean stopLock = new AtomicBoolean(false);
    @Getter
    private Transport transport;
    private Disposable disposable;
    protected final List&lt;Subscriber&lt;INDArray&gt;&gt; updaterParamsSubscribers = new CopyOnWriteArrayList&lt;&gt;();
    protected final List&lt;Subscriber&lt;INDArray&gt;&gt; modelParamsSubsribers = new CopyOnWriteArrayList&lt;&gt;();
    protected final List&lt;UpdatesHandler&gt; updatesSubscribers = new CopyOnWriteArrayList&lt;&gt;();
    private final BlockingQueue&lt;INDArray&gt; updatesQueue = new LinkedBlockingQueue&lt;&gt;(4096);
    private final AtomicBoolean launchLock = new AtomicBoolean(false);

}

