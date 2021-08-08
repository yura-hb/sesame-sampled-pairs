import java.util.ArrayList;

class EvaluationBinary extends BaseEvaluation&lt;EvaluationBinary&gt; {
    /**
     * Set the label names, for printing via {@link #stats()}
     */
    public void setLabelNames(List&lt;String&gt; labels) {
	if (labels == null) {
	    this.labels = null;
	    return;
	}
	this.labels = new ArrayList&lt;&gt;(labels);
    }

    private List&lt;String&gt; labels;

}

