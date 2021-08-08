import org.nd4j.jita.allocator.concurrency.DeviceAllocationsTracker;
import org.nd4j.jita.allocator.enums.AllocationStatus;
import java.util.concurrent.atomic.AtomicLong;

class CudaZeroHandler implements MemoryHandler {
    /**
     * This method returns total amount of memory allocated within system
     *
     * @return
     */
    @Override
    public Table&lt;AllocationStatus, Integer, Long&gt; getAllocationStatistics() {
	Table&lt;AllocationStatus, Integer, Long&gt; table = HashBasedTable.create();
	table.put(AllocationStatus.HOST, 0, zeroUseCounter.get());
	for (Integer deviceId : configuration.getAvailableDevices()) {
	    table.put(AllocationStatus.DEVICE, deviceId, getAllocatedDeviceMemory(deviceId));
	}
	return table;
    }

    protected final AtomicLong zeroUseCounter = new AtomicLong(0);
    private static Configuration configuration = CudaEnvironment.getInstance().getConfiguration();
    protected volatile DeviceAllocationsTracker deviceMemoryTracker;

    /**
     * This method returns total amount of memory allocated at specified device
     *
     * @param device
     * @return
     */
    @Override
    public long getAllocatedDeviceMemory(Integer device) {
	return deviceMemoryTracker.getAllocatedSize(device);
    }

}

