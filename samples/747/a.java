import java.util.List;

class Tree implements Serializable {
    /**
     * Returns whether the node has any children or not
     * @return whether the node has any children or not
     */
    public boolean isLeaf() {
	return children == null || children.isEmpty();
    }

    private List&lt;Tree&gt; children;

}

