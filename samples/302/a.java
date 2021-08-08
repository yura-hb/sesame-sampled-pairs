import java.util.*;

class DifferentialFunctionClassHolder {
    /***
     * Returns the missing onnx ops
     * @return
     */
    public Set&lt;String&gt; missingOnnxOps() {
	Set&lt;String&gt; copy = new HashSet&lt;&gt;(onnxOpDescriptors.keySet());
	copy.removeAll(onnxNames.keySet());
	return copy;
    }

    private Map&lt;String, OpDescriptor&gt; onnxOpDescriptors;
    private Map&lt;String, DifferentialFunction&gt; onnxNames = new HashMap&lt;&gt;();

}

