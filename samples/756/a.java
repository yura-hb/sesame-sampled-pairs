import org.deeplearning4j.models.sequencevectors.graph.enums.NoEdgeHandling;
import org.deeplearning4j.models.sequencevectors.graph.enums.PopularityMode;
import org.deeplearning4j.models.sequencevectors.graph.enums.SpreadSpectrum;
import org.deeplearning4j.models.sequencevectors.graph.enums.WalkDirection;
import org.deeplearning4j.models.sequencevectors.graph.exception.NoEdgesException;
import org.deeplearning4j.models.sequencevectors.graph.primitives.IGraph;
import org.deeplearning4j.models.sequencevectors.graph.primitives.Vertex;
import org.deeplearning4j.models.sequencevectors.sequence.Sequence;
import java.util.*;

class PopularityWalker&lt;T&gt; extends RandomWalker&lt;T&gt; implements GraphWalker&lt;T&gt; {
    /**
     * This method returns next walk sequence from this graph
     *
     * @return
     */
    @Override
    public Sequence&lt;T&gt; next() {
	Sequence&lt;T&gt; sequence = new Sequence&lt;&gt;();
	int[] visitedHops = new int[walkLength];
	Arrays.fill(visitedHops, -1);

	int startPosition = position.getAndIncrement();
	int lastId = -1;
	int startPoint = order[startPosition];
	startPosition = startPoint;
	for (int i = 0; i &lt; walkLength; i++) {

	    Vertex&lt;T&gt; vertex = sourceGraph.getVertex(startPosition);

	    int currentPosition = startPosition;

	    sequence.addElement(vertex.getValue());
	    visitedHops[i] = vertex.vertexID();
	    int cSpread = 0;

	    if (alpha &gt; 0 && lastId != startPoint && lastId != -1 && alpha &gt; rng.nextDouble()) {
		startPosition = startPoint;
		continue;
	    }

	    switch (walkDirection) {
	    case RANDOM:
	    case FORWARD_ONLY:
	    case FORWARD_UNIQUE:
	    case FORWARD_PREFERRED: {

		// ArrayUtils.removeElements(sourceGraph.getConnectedVertexIndices(order[currentPosition]), visitedHops);
		int[] connections = ArrayUtils.removeElements(sourceGraph.getConnectedVertexIndices(vertex.vertexID()),
			visitedHops);

		// we get  popularity of each node connected to the current node.
		PriorityQueue&lt;Node&lt;T&gt;&gt; queue = new PriorityQueue&lt;&gt;(Math.max(10, connections.length),
			new NodeComparator());

		int start = 0;
		int stop = 0;
		int cnt = 0;
		if (connections.length &gt; 0) {

		    for (int connected : connections) {
			Node&lt;T&gt; tNode = new Node&lt;&gt;(connected, sourceGraph.getConnectedVertices(connected).size());
			queue.add(tNode);
		    }

		    cSpread = spread &gt; connections.length ? connections.length : spread;

		    switch (popularityMode) {
		    case MAXIMUM:
			start = 0;
			stop = start + cSpread - 1;
			break;
		    case MINIMUM:
			start = connections.length - cSpread;
			stop = connections.length - 1;
			break;
		    case AVERAGE:
			int mid = connections.length / 2;
			start = mid - (cSpread / 2);
			stop = mid + (cSpread / 2);
			break;
		    }

		    // logger.info("Spread: ["+ cSpread+ "], Connections: ["+ connections.length+"], Start: ["+start+"], Stop: ["+stop+"]");
		    cnt = 0;
		    //logger.info("Queue: " + queue);
		    //logger.info("Queue size: " + queue.size());

		    List&lt;Node&lt;T&gt;&gt; list = new ArrayList&lt;&gt;();
		    double[] weights = new double[cSpread];

		    int fcnt = 0;
		    while (!queue.isEmpty()) {
			Node&lt;T&gt; node = queue.poll();
			if (cnt &gt;= start && cnt &lt;= stop) {
			    list.add(node);
			    weights[fcnt] = node.getWeight();
			    fcnt++;
			}
			connections[cnt] = node.getVertexId();

			cnt++;
		    }

		    int con = -1;

		    switch (spectrum) {
		    case PLAIN: {
			con = RandomUtils.nextInt(start, stop + 1);

			//    logger.info("Picked selection: " + con);

			Vertex&lt;T&gt; nV = sourceGraph.getVertex(connections[con]);
			startPosition = nV.vertexID();
			lastId = vertex.vertexID();
		    }
			break;
		    case PROPORTIONAL: {
			double norm[] = MathArrays.normalizeArray(weights, 1);
			double prob = rng.nextDouble();
			double floor = 0.0;
			for (int b = 0; b &lt; weights.length; b++) {
			    if (prob &gt;= floor && prob &lt; floor + norm[b]) {
				startPosition = list.get(b).getVertexId();
				lastId = startPosition;
				break;
			    } else {
				floor += norm[b];
			    }
			}
		    }
			break;
		    }

		} else {
		    switch (noEdgeHandling) {
		    case EXCEPTION_ON_DISCONNECTED:
			throw new NoEdgesException("No more edges at vertex [" + currentPosition + "]");
		    case CUTOFF_ON_DISCONNECTED:
			i += walkLength;
			break;
		    case SELF_LOOP_ON_DISCONNECTED:
			startPosition = currentPosition;
			break;
		    case RESTART_ON_DISCONNECTED:
			startPosition = startPoint;
			break;
		    default:
			throw new UnsupportedOperationException("Unsupported noEdgeHandling: [" + noEdgeHandling + "]");
		    }
		}
	    }
		break;
	    default:
		throw new UnsupportedOperationException("Unknown WalkDirection: [" + walkDirection + "]");
	    }

	}

	return sequence;
    }

    protected int spread = 10;
    protected PopularityMode popularityMode = PopularityMode.MAXIMUM;
    protected SpreadSpectrum spectrum;

}

