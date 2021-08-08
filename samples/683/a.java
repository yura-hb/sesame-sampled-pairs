import org.nd4j.jita.allocator.concurrency.DeviceAllocationsTracker;
import org.nd4j.jita.flow.FlowController;

class CudaZeroHandler implements MemoryHandler {
    /**
     * This method gets called from Allocator, during Allocator/MemoryHandler initialization
     *
     * @param configuration
     * @param allocator
     */
    @Override
    public void init(@NonNull Configuration configuration, @NonNull Allocator allocator) {
	this.configuration = configuration;

	this.deviceMemoryTracker = new DeviceAllocationsTracker(this.configuration);
	this.flowController.init(allocator);
    }

    private static Configuration configuration = CudaEnvironment.getInstance().getConfiguration();
    protected volatile DeviceAllocationsTracker deviceMemoryTracker;
    private final FlowController flowController;

}

