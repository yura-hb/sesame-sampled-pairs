import org.nd4j.jita.allocator.Allocator;
import org.nd4j.jita.allocator.pointers.cuda.cudaEvent_t;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

class AsynchronousFlowController implements FlowController {
    /**
     * This method ensures the events in the beginning of FIFO queues are finished
     */
    protected void sweepTail() {
	Integer deviceId = allocator.getDeviceId();
	int cnt = 0;

	// we get number of issued commands for specific device
	long lastCommandId = deviceClocks.get(deviceId).get();

	for (int l = 0; l &lt; configuration.getCommandLanesNumber(); l++) {
	    Queue&lt;cudaEvent_t&gt; queue = eventsBarrier.get(deviceId).get(l);

	    if (queue.size() &gt;= MAX_EXECUTION_QUEUE
		    || laneClocks.get(deviceId).get(l).get() &lt; lastCommandId - MAX_EXECUTION_QUEUE) {
		cudaEvent_t event = queue.poll();
		if (event != null && !event.isDestroyed()) {
		    event.synchronize();
		    event.destroy();
		    cnt++;
		}
	    }

	}

	deviceClocks.get(deviceId).incrementAndGet();

	//  log.info("Events sweeped: [{}]", cnt);
    }

    private volatile Allocator allocator;
    protected ArrayList&lt;AtomicLong&gt; deviceClocks = new ArrayList&lt;&gt;();
    private static final Configuration configuration = CudaEnvironment.getInstance().getConfiguration();
    protected ArrayList&lt;ArrayList&lt;Queue&lt;cudaEvent_t&gt;&gt;&gt; eventsBarrier = new ArrayList&lt;&gt;();
    protected static final int MAX_EXECUTION_QUEUE = configuration.getCommandQueueLength();
    protected ArrayList&lt;ArrayList&lt;AtomicLong&gt;&gt; laneClocks = new ArrayList&lt;&gt;();

}

