import java.lang.management.ManagementFactory;
import com.sun.management.ThreadMXBean;

class SurvivorAlignmentTestMain {
    /**
     * Baselines amount of memory allocated by each thread.
     */
    public void baselineMemoryAllocation() {
	ThreadMXBean bean = (ThreadMXBean) ManagementFactory.getThreadMXBean();
	threadIds = bean.getAllThreadIds();
	baselinedThreadMemoryUsage = bean.getThreadAllocatedBytes(threadIds);
    }

    private long[] threadIds = null;
    private long[] baselinedThreadMemoryUsage = null;

}

