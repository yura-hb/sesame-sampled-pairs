import org.nd4j.linalg.primitives.Counter;
import java.util.*;

class Evaluation extends BaseEvaluation&lt;Evaluation&gt; {
    /**
     * False positive: wrong guess
     *
     * @return the count of the false positives
     */
    public Map&lt;Integer, Integer&gt; falsePositives() {
	return convertToMap(falsePositives, confusion().getClasses().size());
    }

    protected Counter&lt;Integer&gt; falsePositives = new Counter&lt;&gt;();
    @JsonSerialize(using = ConfusionMatrixSerializer.class)
    @JsonDeserialize(using = ConfusionMatrixDeserializer.class)
    protected ConfusionMatrix&lt;Integer&gt; confusion;

    private ConfusionMatrix&lt;Integer&gt; confusion() {
	return confusion;
    }

    private Map&lt;Integer, Integer&gt; convertToMap(Counter&lt;Integer&gt; counter, int maxCount) {
	Map&lt;Integer, Integer&gt; map = new HashMap&lt;&gt;();
	for (int i = 0; i &lt; maxCount; i++) {
	    map.put(i, (int) counter.getCount(i));
	}
	return map;
    }

}

