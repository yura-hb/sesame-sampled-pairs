import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.graph.FlatResult;
import org.nd4j.graph.FlatVariable;
import org.nd4j.linalg.api.memory.pointers.PagedPointer;
import org.nd4j.linalg.exception.ND4JIllegalStateException;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.nativeblas.NativeOpsHolder;
import java.util.HashMap;
import java.util.Map;

class NativeGraphExecutioner implements GraphExecutioner {
    /**
     * This method executes given graph and returns results
     *
     * @param sd
     * @return
     */
    @Override
    public INDArray[] executeGraph(SameDiff sd, ExecutorConfiguration configuration) {

	Map&lt;Integer, Node&gt; intermediate = new HashMap&lt;&gt;();

	ByteBuffer buffer = convertToFlatBuffers(sd, configuration, intermediate);

	BytePointer bPtr = new BytePointer(buffer);

	log.info("Buffer length: {}", buffer.limit());

	val res = NativeOpsHolder.getInstance().getDeviceNativeOps().executeFlatGraphFloat(null, bPtr);
	if (res == null)
	    throw new ND4JIllegalStateException("Graph execution failed");

	PagedPointer pagedPointer = new PagedPointer(res.pointer(), res.size());
	FlatResult fr = FlatResult.getRootAsFlatResult(pagedPointer.asBytePointer().asByteBuffer());

	log.info("VarMap: {}", sd.variableMap());

	INDArray[] results = new INDArray[fr.variablesLength()];

	for (int e = 0; e &lt; fr.variablesLength(); e++) {
	    FlatVariable var = fr.variables(e);
	    //            log.info("Var received: id: [{}:{}/&lt;{}&gt;];", var.id().first(), var.id().second(), var.name());
	    FlatArray ndarray = var.ndarray();

	    INDArray val = Nd4j.createFromFlatArray(ndarray);
	    results[e] = val;

	    if (var.name() != null && sd.variableMap().containsKey(var.name())) {
		sd.associateArrayWithVariable(val, sd.variableMap().get(var.name()));
	    } else {
		if (sd.variableMap().get(var.name()) != null) {
		    sd.associateArrayWithVariable(val, sd.getVariable(var.name()));
		} else {
		    log.warn("Unknown variable received: [{}]", var.name());
		}
	    }
	}

	// now we need to release native memory
	NativeOpsHolder.getInstance().getDeviceNativeOps().deleteResultWrapper(res);

	return results;
    }

    public ByteBuffer convertToFlatBuffers(SameDiff sd, ExecutorConfiguration configuration,
	    Map&lt;Integer, Node&gt; intermediate) {
	log.info("Configuration: {}", configuration);

	return sd.asFlatBuffers(configuration);
    }

}

