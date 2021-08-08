import org.nd4j.jita.handler.MemoryHandler;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.INDArray;
import java.util.Map;

class AtomicAllocator implements Allocator {
    /**
     * This method should be called to make sure that data on host side is actualized
     *
     * @param array
     */
    @Override
    public void synchronizeHostData(INDArray array) {
	DataBuffer buffer = array.data().originalDataBuffer() == null ? array.data()
		: array.data().originalDataBuffer();
	synchronizeHostData(buffer);
    }

    @Getter
    private transient MemoryHandler memoryHandler;
    private Map&lt;Long, AllocationPoint&gt; allocationsMap = new ConcurrentHashMap&lt;&gt;();

    /**
     * This method should be called to make sure that data on host side is actualized
     *
     * @param buffer
     */
    @Override
    public void synchronizeHostData(DataBuffer buffer) {
	// we don't want non-committed ops left behind
	//Nd4j.getExecutioner().push();

	// we don't synchronize constant buffers, since we assume they are always valid on host side
	if (buffer.isConstant()) {
	    return;
	}

	// we actually need synchronization only in device-dependant environment. no-op otherwise
	if (memoryHandler.isDeviceDependant()) {
	    AllocationPoint point = getAllocationPoint(buffer.getTrackingPoint());
	    if (point == null)
		throw new RuntimeException("AllocationPoint is NULL");
	    memoryHandler.synchronizeThreadDevice(Thread.currentThread().getId(), memoryHandler.getDeviceId(), point);
	}
    }

    /**
     * This method returns AllocationPoint POJO for specified tracking ID
     * @param objectId
     * @return
     */
    protected AllocationPoint getAllocationPoint(Long objectId) {
	return allocationsMap.get(objectId);
    }

}

