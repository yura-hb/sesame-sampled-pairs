import org.nd4j.jita.allocator.pointers.CudaPointer;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.nativeblas.NativeOpsHolder;
import java.util.Map;

class CudaEnvironment {
    /**
     * Get the current device architecture
     * @return the major/minor version of
     * the current device
     */
    public int getCurrentDeviceArchitecture() {
	int deviceId = Nd4j.getAffinityManager().getDeviceForCurrentThread();
	if (!arch.containsKey(deviceId)) {
	    int major = NativeOpsHolder.getInstance().getDeviceNativeOps().getDeviceMajor(new CudaPointer(deviceId));
	    int minor = NativeOpsHolder.getInstance().getDeviceNativeOps().getDeviceMinor(new CudaPointer(deviceId));
	    Integer cc = Integer.parseInt(new String("" + major + minor));
	    arch.put(deviceId, cc);
	    return cc;
	}

	return arch.get(deviceId);
    }

    private static Map&lt;Integer, Integer&gt; arch = new ConcurrentHashMap&lt;&gt;();

}

