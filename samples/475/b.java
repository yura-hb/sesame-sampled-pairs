import org.graalvm.compiler.graph.Graph;
import org.graalvm.compiler.graph.Node;

class GraphUtil {
    /**
     * Removes all nodes created after the {@code mark}, assuming no "old" nodes point to "new"
     * nodes.
     */
    public static void removeNewNodes(Graph graph, Graph.Mark mark) {
	assert checkNoOldToNewEdges(graph, mark);
	for (Node n : graph.getNewNodes(mark)) {
	    n.markDeleted();
	    for (Node in : n.inputs()) {
		in.removeUsage(n);
	    }
	}
    }

    private static boolean checkNoOldToNewEdges(Graph graph, Graph.Mark mark) {
	for (Node old : graph.getNodes()) {
	    if (graph.isNew(mark, old)) {
		break;
	    }
	    for (Node n : old.successors()) {
		assert !graph.isNew(mark, n) : old + " -&gt; " + n;
	    }
	    for (Node n : old.inputs()) {
		assert !graph.isNew(mark, n) : old + " -&gt; " + n;
	    }
	}
	return true;
    }

}

