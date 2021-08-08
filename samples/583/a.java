import org.nd4j.linalg.api.buffer.DataBuffer;

class AllocUtil {
    /**
     * Get the allocation mode from the context
     * @return
     */
    public static DataBuffer.AllocationMode getAllocationModeFromContext(String allocMode) {
	switch (allocMode) {
	case "heap":
	    return DataBuffer.AllocationMode.HEAP;
	case "javacpp":
	    return DataBuffer.AllocationMode.JAVACPP;
	case "direct":
	    return DataBuffer.AllocationMode.DIRECT;
	default:
	    return DataBuffer.AllocationMode.JAVACPP;
	}
    }

}

