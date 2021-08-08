import java.util.*;

class Evaluation extends BaseEvaluation&lt;Evaluation&gt; {
    /**
     * Get a String representation of the confusion matrix
     */
    public String confusionToString() {
	int nClasses = confusion().getClasses().size();

	//First: work out the longest label size
	int maxLabelSize = 0;
	for (String s : labelsList) {
	    maxLabelSize = Math.max(maxLabelSize, s.length());
	}

	//Build the formatting for the rows:
	int labelSize = Math.max(maxLabelSize + 5, 10);
	StringBuilder sb = new StringBuilder();
	sb.append("%-3d");
	sb.append("%-");
	sb.append(labelSize);
	sb.append("s | ");

	StringBuilder headerFormat = new StringBuilder();
	headerFormat.append("   %-").append(labelSize).append("s   ");

	for (int i = 0; i &lt; nClasses; i++) {
	    sb.append("%7d");
	    headerFormat.append("%7d");
	}
	String rowFormat = sb.toString();

	StringBuilder out = new StringBuilder();
	//First: header row
	Object[] headerArgs = new Object[nClasses + 1];
	headerArgs[0] = "Predicted:";
	for (int i = 0; i &lt; nClasses; i++)
	    headerArgs[i + 1] = i;
	out.append(String.format(headerFormat.toString(), headerArgs)).append("\n");

	//Second: divider rows
	out.append("   Actual:\n");

	//Finally: data rows
	for (int i = 0; i &lt; nClasses; i++) {

	    Object[] args = new Object[nClasses + 2];
	    args[0] = i;
	    args[1] = labelsList.get(i);
	    for (int j = 0; j &lt; nClasses; j++) {
		args[j + 2] = confusion().getCount(i, j);
	    }
	    out.append(String.format(rowFormat, args));
	    out.append("\n");
	}

	return out.toString();
    }

    @Getter
    @Setter
    protected List&lt;String&gt; labelsList = new ArrayList&lt;&gt;();
    @JsonSerialize(using = ConfusionMatrixSerializer.class)
    @JsonDeserialize(using = ConfusionMatrixDeserializer.class)
    protected ConfusionMatrix&lt;Integer&gt; confusion;

    private ConfusionMatrix&lt;Integer&gt; confusion() {
	return confusion;
    }

}

