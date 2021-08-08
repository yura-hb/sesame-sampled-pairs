abstract class EndpointPair&lt;N&gt; implements Iterable&lt;N&gt; {
    /**
    * Returns the node that is adjacent to {@code node} along the origin edge.
    *
    * @throws IllegalArgumentException if this {@link EndpointPair} does not contain {@code node}
    */
    public final N adjacentNode(Object node) {
	if (node.equals(nodeU)) {
	    return nodeV;
	} else if (node.equals(nodeV)) {
	    return nodeU;
	} else {
	    throw new IllegalArgumentException("EndpointPair " + this + " does not contain node " + node);
	}
    }

    private final N nodeU;
    private final N nodeV;

}

