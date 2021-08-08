import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.instrumentation.Instrumentation;
import org.nd4j.linalg.api.ndarray.*;
import org.nd4j.linalg.api.shape.Shape;
import org.nd4j.linalg.exception.ND4JIllegalStateException;
import java.util.*;

class Nd4j {
    /**
     *
     * @param data
     * @param shapeInfo
     * @return
     */
    public static INDArray createArrayFromShapeBuffer(DataBuffer data, DataBuffer shapeInfo) {
	int rank = Shape.rank(shapeInfo);
	long offset = 0;
	INDArray result = Nd4j.create(data, toIntArray(rank, Shape.shapeOf(shapeInfo)),
		toIntArray(rank, Shape.stride(shapeInfo)), offset, Shape.order(shapeInfo));
	if (data instanceof CompressedDataBuffer)
	    result.markAsCompressed(true);

	return result;
    }

    protected static NDArrayFactory INSTANCE;
    public static boolean shouldInstrument = false;
    protected static Instrumentation instrumentation;

    private static int[] toIntArray(int length, DataBuffer buffer) {
	int[] ret = new int[length];
	for (int i = 0; i &lt; length; i++) {
	    ret[i] = buffer.getInt(i);
	}
	return ret;
    }

    /**
     *
     * @param data
     * @param newShape
     * @param newStride
     * @param offset
     * @param ordering
     * @return
     */
    public static INDArray create(DataBuffer data, int[] newShape, int[] newStride, long offset, char ordering) {
	checkShapeValues(newShape);

	INDArray ret = INSTANCE.create(data, newShape, newStride, offset, ordering);
	logCreationIfNecessary(ret);
	return ret;
    }

    /**
     *
     * @param shape
     */
    public static void checkShapeValues(int[] shape) {
	for (int e : shape) {
	    if (e &lt; 1)
		throw new ND4JIllegalStateException("Invalid shape: Requested INDArray shape " + Arrays.toString(shape)
			+ " contains dimension size values &lt; 1 (all dimensions must be 1 or more)");
	}
    }

    private static void logCreationIfNecessary(INDArray log) {
	if (shouldInstrument)
	    Nd4j.getInstrumentation().log(log);
    }

    /**
     * Gets the instrumentation instance
     *
     * @return the instrumentation instance
     */
    public static Instrumentation getInstrumentation() {
	return instrumentation;
    }

}

