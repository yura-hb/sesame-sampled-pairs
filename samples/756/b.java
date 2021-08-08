import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class ModuleHashesBuilder {
    class Graph&lt;T&gt; {
	/**
	 * Returns a transposed graph from this graph.
	 */
	public Graph&lt;T&gt; transpose() {
	    Builder&lt;T&gt; builder = new Builder&lt;&gt;();
	    nodes.forEach(builder::addNode);
	    // reverse edges
	    edges.keySet().forEach(u -&gt; {
		edges.get(u).forEach(v -&gt; builder.addEdge(v, u));
	    });
	    return builder.build();
	}

	private final Set&lt;T&gt; nodes;
	private final Map&lt;T, Set&lt;T&gt;&gt; edges;

	public Graph(Set&lt;T&gt; nodes, Map&lt;T, Set&lt;T&gt;&gt; edges) {
	    this.nodes = Collections.unmodifiableSet(nodes);
	    this.edges = Collections.unmodifiableMap(edges);
	}

	class Builder&lt;T&gt; {
	    private final Set&lt;T&gt; nodes;
	    private final Map&lt;T, Set&lt;T&gt;&gt; edges;

	    public void addNode(T node) {
		if (nodes.add(node)) {
		    edges.computeIfAbsent(node, _e -&gt; new HashSet&lt;&gt;());
		}
	    }

	    public void addEdge(T u, T v) {
		addNode(u);
		addNode(v);
		edges.get(u).add(v);
	    }

	    public Graph&lt;T&gt; build() {
		return new Graph&lt;T&gt;(nodes, edges);
	    }

	}

    }

}

