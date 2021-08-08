import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

class ProtectedCudaConstantHandler implements ConstantHandler {
    /**
     * This method removes all cached constants
     */
    @Override
    public void purgeConstants() {
	buffersCache = new HashMap&lt;&gt;();

	protector.purgeProtector();

	resetHappened = true;
	logger.info("Resetting Constants...");

	for (Integer device : constantOffsets.keySet()) {
	    constantOffsets.get(device).set(0);
	    buffersCache.put(device, new ConcurrentHashMap&lt;ArrayDescriptor, DataBuffer&gt;());
	}
    }

    protected Map&lt;Integer, Map&lt;ArrayDescriptor, DataBuffer&gt;&gt; buffersCache = new HashMap&lt;&gt;();
    protected static final ConstantProtector protector = ConstantProtector.getInstance();
    private boolean resetHappened = false;
    private static Logger logger = LoggerFactory.getLogger(ProtectedCudaConstantHandler.class);
    protected Map&lt;Integer, AtomicLong&gt; constantOffsets = new HashMap&lt;&gt;();

}

