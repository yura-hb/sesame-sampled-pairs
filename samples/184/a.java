import java.util.Map;

class PathMap&lt;T&gt; {
    /**
     * Returns the value associated with the given key
     */
    public T get(IPath key) {
	Node&lt;T&gt; node = this.root.getMostSpecificNode(key);
	if (!node.exists || node.depth &lt; key.segmentCount()) {
	    return null;
	}
	return node.value;
    }

    private Node&lt;T&gt; root = new DeviceNode&lt;T&gt;();

    class Node&lt;T&gt; {
	private Node&lt;T&gt; root = new DeviceNode&lt;T&gt;();

	/**
		 * Returns this node or one of its descendants whose path is the longest-possible prefix of the given key (or
		 * equal to it).
		 */
	public Node&lt;T&gt; getMostSpecificNode(IPath key) {
	    if (this.depth == key.segmentCount()) {
		return this;
	    }
	    String nextSegment = getSegment(key);

	    Node&lt;T&gt; child = getChild(nextSegment);
	    if (child == null) {
		return this;
	    }
	    Node&lt;T&gt; result = child.getMostSpecificNode(key);
	    if (result.exists) {
		return result;
	    } else {
		return this;
	    }
	}

	String getSegment(IPath key) {
	    return key.segment(this.depth);
	}

	Node&lt;T&gt; getChild(String nextSegment) {
	    if (this.children == null) {
		return null;
	    }
	    return this.children.get(nextSegment);
	}

    }

}

