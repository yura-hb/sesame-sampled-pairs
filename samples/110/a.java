import org.deeplearning4j.clustering.sptree.HeapObject;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.accum.distances.*;
import org.nd4j.linalg.factory.Nd4j;
import java.util.*;

class VPTree implements Serializable {
    /**
     *
     * @param node
     * @param target
     * @param k
     * @param pq
     */
    public void search(Node node, INDArray target, int k, PriorityQueue&lt;HeapObject&gt; pq, double cTau) {

	if (node == null)
	    return;

	double tau = cTau;

	INDArray get = node.getPoint(); //items.getRow(node.getIndex());
	double distance = distance(get, target);
	if (distance &lt; tau) {
	    if (pq.size() == k)
		pq.poll();

	    pq.add(new HeapObject(node.getIndex(), node.getPoint(), distance));
	    if (pq.size() == k)
		tau = pq.peek().getDistance();
	}

	Node left = node.getLeft();
	Node right = node.getRight();

	if (left == null && right == null)
	    return;

	if (distance &lt; node.getThreshold()) {
	    if (distance - tau &lt; node.getThreshold()) { // if there can still be neighbors inside the ball, recursively search left child first
		search(left, target, k, pq, tau);
	    }

	    if (distance + tau &gt;= node.getThreshold()) { // if there can still be neighbors outside the ball, recursively search right child
		search(right, target, k, pq, tau);
	    }

	} else {
	    if (distance + tau &gt;= node.getThreshold()) { // if there can still be neighbors outside the ball, recursively search right child first
		search(right, target, k, pq, tau);
	    }

	    if (distance - tau &lt; node.getThreshold()) { // if there can still be neighbors inside the ball, recursively search left child
		search(left, target, k, pq, tau);
	    }
	}

    }

    private transient ThreadLocal&lt;INDArray&gt; scalars = new ThreadLocal&lt;&gt;();
    private String similarityFunction;
    @Getter
    private boolean invert = false;

    /**
     * Euclidean distance
     * @return the distance between the two points
     */
    public float distance(INDArray arr1, INDArray arr2) {
	if (scalars == null)
	    scalars = new ThreadLocal&lt;&gt;();

	if (scalars.get() == null)
	    scalars.set(Nd4j.scalar(0.0));

	switch (similarityFunction) {
	case "jaccard":
	    float ret7 = Nd4j.getExecutioner()
		    .execAndReturn(new JaccardDistance(arr1, arr2, scalars.get(), arr1.length())).getFinalResult()
		    .floatValue();
	    return invert ? -ret7 : ret7;
	case "hamming":
	    float ret8 = Nd4j.getExecutioner()
		    .execAndReturn(new HammingDistance(arr1, arr2, scalars.get(), arr1.length())).getFinalResult()
		    .floatValue();
	    return invert ? -ret8 : ret8;
	case "euclidean":
	    float ret = Nd4j.getExecutioner()
		    .execAndReturn(new EuclideanDistance(arr1, arr2, scalars.get(), arr1.length())).getFinalResult()
		    .floatValue();
	    return invert ? -ret : ret;
	case "cosinesimilarity":
	    float ret2 = Nd4j.getExecutioner()
		    .execAndReturn(new CosineSimilarity(arr1, arr2, scalars.get(), arr1.length())).getFinalResult()
		    .floatValue();
	    return invert ? -ret2 : ret2;
	case "cosinedistance":
	    float ret6 = Nd4j.getExecutioner()
		    .execAndReturn(new CosineDistance(arr1, arr2, scalars.get(), arr1.length())).getFinalResult()
		    .floatValue();
	    return invert ? -ret6 : ret6;
	case "manhattan":
	    float ret3 = Nd4j.getExecutioner()
		    .execAndReturn(new ManhattanDistance(arr1, arr2, scalars.get(), arr1.length())).getFinalResult()
		    .floatValue();
	    return invert ? -ret3 : ret3;
	case "dot":
	    float dotRet = (float) Nd4j.getBlasWrapper().dot(arr1, arr2);
	    return invert ? -dotRet : dotRet;
	default:
	    float ret4 = Nd4j.getExecutioner()
		    .execAndReturn(new EuclideanDistance(arr1, arr2, scalars.get(), arr1.length())).getFinalResult()
		    .floatValue();
	    return invert ? -ret4 : ret4;

	}
    }

}

