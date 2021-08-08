import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.split.InputStreamInputSplit;
import org.nd4j.linalg.api.ops.performance.PerformanceTracker;
import org.nd4j.linalg.memory.MemcpyDirection;
import java.util.Map;

class IOTiming {
    /**
     *
     * @param reader
     * @param inputStream
     * @param function
     * @return
     * @throws Exception
     */
    public static TimingStatistics timeNDArrayCreation(RecordReader reader, InputStream inputStream,
	    INDArrayCreationFunction function) throws Exception {

	reader.initialize(new InputStreamInputSplit(inputStream));
	long longNanos = System.nanoTime();
	List&lt;Writable&gt; next = reader.next();
	long endNanos = System.nanoTime();
	long etlDiff = endNanos - longNanos;
	long startArrCreation = System.nanoTime();
	INDArray arr = function.createFromRecord(next);
	long endArrCreation = System.nanoTime();
	long endCreationDiff = endArrCreation - startArrCreation;
	Map&lt;Integer, Map&lt;MemcpyDirection, Long&gt;&gt; currentBandwidth = PerformanceTracker.getInstance()
		.getCurrentBandwidth();
	val bw = currentBandwidth.get(0).get(MemcpyDirection.HOST_TO_DEVICE);
	val deviceToHost = currentBandwidth.get(0).get(MemcpyDirection.HOST_TO_DEVICE);

	return TimingStatistics.builder().diskReadingTimeNanos(etlDiff).bandwidthNanosHostToDevice(bw)
		.bandwidthDeviceToHost(deviceToHost).ndarrayCreationTimeNanos(endCreationDiff).build();
    }

    interface INDArrayCreationFunction {
	INDArray createFromRecord(List&lt;Writable&gt; record);

    }

}

