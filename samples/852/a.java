import java.util.*;

class Sequence&lt;T&gt; implements Serializable {
    /**
     * Returns this sequence as list of labels
     * @return
     */
    public List&lt;String&gt; asLabels() {
	List&lt;String&gt; labels = new ArrayList&lt;&gt;();
	for (T element : getElements()) {
	    labels.add(element.getLabel());
	}
	return labels;
    }

    protected List&lt;T&gt; elements = new ArrayList&lt;&gt;();

    /**
     * Returns an ordered unmodifiable list of elements from this sequence
     *
     * @return
     */
    public List&lt;T&gt; getElements() {
	return Collections.unmodifiableList(elements);
    }

}

