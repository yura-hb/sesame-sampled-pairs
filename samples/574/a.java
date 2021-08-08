import org.nd4j.jita.allocator.impl.AtomicAllocator;
import org.nd4j.nativeblas.NativeOpsHolder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

class ProtectedCudaConstantHandler implements ConstantHandler {
    /**
     * Method suited for debug purposes only
     *
     * @return
     */
    protected int amountOfEntries(int deviceId) {
	ensureMaps(deviceId);
	return buffersCache.get(0).size();
    }

    protected Map&lt;Integer, Map&lt;ArrayDescriptor, DataBuffer&gt;&gt; buffersCache = new HashMap&lt;&gt;();
    protected FlowController flowController;
    protected Map&lt;Integer, AtomicLong&gt; constantOffsets = new HashMap&lt;&gt;();
    protected Map&lt;Integer, Semaphore&gt; deviceLocks = new ConcurrentHashMap&lt;&gt;();
    protected Map&lt;Integer, Pointer&gt; deviceAddresses = new HashMap&lt;&gt;();

    private void ensureMaps(Integer deviceId) {
	if (!buffersCache.containsKey(deviceId)) {
	    if (flowController == null)
		flowController = AtomicAllocator.getInstance().getFlowController();

	    try {
		synchronized (this) {
		    if (!buffersCache.containsKey(deviceId)) {

			// TODO: this op call should be checked
			//nativeOps.setDevice(new CudaPointer(deviceId));

			buffersCache.put(deviceId, new ConcurrentHashMap&lt;ArrayDescriptor, DataBuffer&gt;());
			constantOffsets.put(deviceId, new AtomicLong(0));
			deviceLocks.put(deviceId, new Semaphore(1));

			Pointer cAddr = NativeOpsHolder.getInstance().getDeviceNativeOps().getConstantSpace();
			//                    logger.info("constant pointer: {}", cAddr.address() );

			deviceAddresses.put(deviceId, cAddr);
		    }
		}
	    } catch (Exception e) {
		throw new RuntimeException(e);
	    }
	}
    }

}

