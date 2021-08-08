import java.util.ArrayList;

class ConfusionMatrix&lt;T&gt; implements Serializable {
    /**
     * Gives the applyTransformToDestination of all classes in the confusion matrix.
     */
    public List&lt;T&gt; getClasses() {
	if (classes == null)
	    classes = new ArrayList&lt;&gt;();
	return classes;
    }

    private List&lt;T&gt; classes;

}

