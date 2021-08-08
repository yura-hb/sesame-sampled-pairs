import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.conf.stepfunctions.StepFunction;
import java.util.*;

class NeuralNetConfiguration implements Serializable, Cloneable {
    /**
     * Creates and returns a deep copy of the configuration.
     */
    @Override
    public NeuralNetConfiguration clone() {
	try {
	    NeuralNetConfiguration clone = (NeuralNetConfiguration) super.clone();
	    if (clone.layer != null)
		clone.layer = clone.layer.clone();
	    if (clone.stepFunction != null)
		clone.stepFunction = clone.stepFunction.clone();
	    if (clone.variables != null)
		clone.variables = new ArrayList&lt;&gt;(clone.variables);
	    return clone;
	} catch (CloneNotSupportedException e) {
	    throw new RuntimeException(e);
	}
    }

    protected Layer layer;
    protected StepFunction stepFunction;
    protected List&lt;String&gt; variables = new ArrayList&lt;&gt;();

}

