import java.util.List;

class NodesSet&lt;S&gt; implements Iterable&lt;BSPTree&lt;S&gt;&gt; {
    /** Add a node if not already known.
     * @param node node to add
     */
    public void add(final BSPTree&lt;S&gt; node) {

	for (final BSPTree&lt;S&gt; existing : list) {
	    if (node == existing) {
		// the node is already known, don't add it
		return;
	    }
	}

	// the node was not known, add it
	list.add(node);

    }

    /** List of sub-hyperplanes. */
    private final List&lt;BSPTree&lt;S&gt;&gt; list;

}

