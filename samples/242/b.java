import org.nd4j.linalg.primitives.Pair;

class ClusterSet implements Serializable {
    /**
     *
     * @param point
     * @return
     */
    public Pair&lt;Cluster, Double&gt; nearestCluster(Point point) {

	Cluster nearestCluster = null;
	double minDistance = isInverse() ? Float.MIN_VALUE : Float.MAX_VALUE;

	double currentDistance;
	for (Cluster cluster : getClusters()) {
	    currentDistance = cluster.getDistanceToCenter(point);
	    if (isInverse()) {
		if (currentDistance &gt; minDistance) {
		    minDistance = currentDistance;
		    nearestCluster = cluster;
		}
	    } else {
		if (currentDistance &lt; minDistance) {
		    minDistance = currentDistance;
		    nearestCluster = cluster;
		}
	    }

	}

	return Pair.of(nearestCluster, minDistance);

    }

    private boolean inverse;

    public boolean isInverse() {
	return inverse;
    }

}

