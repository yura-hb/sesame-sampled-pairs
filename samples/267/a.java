import java.util.*;

class Sequence&lt;T&gt; implements Serializable {
    /**
     * Set sequence label
     *
     * @param label
     */
    public void setSequenceLabel(@NonNull T label) {
	this.label = label;
	if (!labels.contains(label))
	    labels.add(label);
    }

    protected T label;
    protected List&lt;T&gt; labels = new ArrayList&lt;&gt;();

}

