import java.util.List;

class Cluster implements Serializable {
    /**
     * Whether the cluster is empty or not
     * @return
     */
    public boolean isEmpty() {
	return points == null || points.isEmpty();
    }

    private List&lt;Point&gt; points = Collections.synchronizedList(new ArrayList&lt;Point&gt;());

}

