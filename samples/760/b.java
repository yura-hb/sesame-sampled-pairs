import com.sun.tools.javac.util.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Type.*;

class Infer {
    class InferenceStep extends Enum&lt;InferenceStep&gt; {
	/**
	 * Can the inference variable be instantiated using this step?
	 */
	public boolean accepts(UndetVar t, InferenceContext inferenceContext) {
	    return filterBounds(t, inferenceContext).nonEmpty() && !t.isCaptured();
	}

	final InferenceBound ib;

	/**
	 * Return the subset of ground bounds in a given bound set (i.e. eq/lower/upper)
	 */
	List&lt;Type&gt; filterBounds(UndetVar uv, InferenceContext inferenceContext) {
	    return Type.filter(uv.getBounds(ib), new BoundFilter(inferenceContext));
	}

    }

    class BoundFilter implements Filter&lt;Type&gt; {
	public BoundFilter(InferenceContext inferenceContext) {
	    this.inferenceContext = inferenceContext;
	}

    }

}

