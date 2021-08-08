import org.nd4j.jita.allocator.enums.Aggressiveness;
import org.nd4j.jita.allocator.enums.AllocationStatus;
import org.nd4j.jita.allocator.time.Ring;
import org.nd4j.jita.allocator.utils.AllocationUtils;
import org.nd4j.jita.flow.FlowController;
import org.nd4j.jita.handler.MemoryHandler;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

class AtomicAllocator implements Allocator {
    /**
     * This method seeks for unused zero-copy memory allocations
     *
     * @param bucketId Id of the bucket, serving allocations
     * @return size of memory that was deallocated
     */
    protected synchronized long seekUnusedZero(Long bucketId, Aggressiveness aggressiveness) {
	AtomicLong freeSpace = new AtomicLong(0);

	int totalElements = (int) memoryHandler.getAllocatedHostObjects(bucketId);

	// these 2 variables will contain jvm-wise memory access frequencies
	float shortAverage = zeroShort.getAverage();
	float longAverage = zeroLong.getAverage();

	// threshold is calculated based on agressiveness specified via configuration
	float shortThreshold = shortAverage / (Aggressiveness.values().length - aggressiveness.ordinal());
	float longThreshold = longAverage / (Aggressiveness.values().length - aggressiveness.ordinal());

	// simple counter for dereferenced objects
	AtomicInteger elementsDropped = new AtomicInteger(0);
	AtomicInteger elementsSurvived = new AtomicInteger(0);

	for (Long object : memoryHandler.getHostTrackingPoints(bucketId)) {
	    AllocationPoint point = getAllocationPoint(object);

	    // point can be null, if memory was promoted to device and was deleted there
	    if (point == null)
		continue;

	    if (point.getAllocationStatus() == AllocationStatus.HOST) {
		//point.getAccessState().isToeAvailable()
		//point.getAccessState().requestToe();

		/*
		    Check if memory points to non-existant buffer, using externals.
		    If externals don't have specified buffer - delete reference.
		 */
		if (point.getBuffer() == null) {
		    purgeZeroObject(bucketId, object, point, false);
		    freeSpace.addAndGet(AllocationUtils.getRequiredMemory(point.getShape()));

		    elementsDropped.incrementAndGet();
		    continue;
		} else {
		    elementsSurvived.incrementAndGet();
		}

		//point.getAccessState().releaseToe();
	    } else {
		//  log.warn("SKIPPING :(");
	    }
	}

	//log.debug("Short average: ["+shortAverage+"], Long average: [" + longAverage + "]");
	//log.debug("Aggressiveness: ["+ aggressiveness+"]; Short threshold: ["+shortThreshold+"]; Long threshold: [" + longThreshold + "]");
	log.debug("Zero {} elements checked: [{}], deleted: {}, survived: {}", bucketId, totalElements,
		elementsDropped.get(), elementsSurvived.get());

	return freeSpace.get();
    }

    @Getter
    private transient MemoryHandler memoryHandler;
    private final Ring zeroShort = new LockedRing(30);
    private final Ring zeroLong = new LockedRing(30);
    private static Logger log = LoggerFactory.getLogger(AtomicAllocator.class);
    private Map&lt;Long, AllocationPoint&gt; allocationsMap = new ConcurrentHashMap&lt;&gt;();

    /**
     * This method returns AllocationPoint POJO for specified tracking ID
     * @param objectId
     * @return
     */
    protected AllocationPoint getAllocationPoint(Long objectId) {
	return allocationsMap.get(objectId);
    }

    /**
     * This method frees native system memory referenced by specified tracking id/AllocationPoint
     *
     * @param bucketId
     * @param objectId
     * @param point
     * @param copyback
     */
    protected void purgeZeroObject(Long bucketId, Long objectId, AllocationPoint point, boolean copyback) {
	allocationsMap.remove(objectId);

	memoryHandler.purgeZeroObject(bucketId, objectId, point, copyback);

	getFlowController().getEventsProvider().storeEvent(point.getLastWriteEvent());
	getFlowController().getEventsProvider().storeEvent(point.getLastReadEvent());
    }

    @Override
    public FlowController getFlowController() {
	return memoryHandler.getFlowController();
    }

}

