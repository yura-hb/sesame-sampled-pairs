import org.deeplearning4j.clustering.info.ClusterSetInfo;
import java.util.*;

class ClusterUtils {
    /**
     *
     * @param clusterSet
     * @param info
     * @param maximumDistance
     * @return
     */
    public static List&lt;Cluster&gt; getClustersWhereMaximumDistanceFromCenterGreaterThan(final ClusterSet clusterSet,
	    final ClusterSetInfo info, double maximumDistance) {
	List&lt;Cluster&gt; clusters = new ArrayList&lt;&gt;();
	for (Cluster cluster : clusterSet.getClusters()) {
	    ClusterInfo clusterInfo = info.getClusterInfo(cluster.getId());
	    if (clusterInfo != null) {
		if (clusterInfo.isInverse() && clusterInfo.getMaxPointDistanceFromCenter() &lt; maximumDistance) {
		    clusters.add(cluster);
		} else if (clusterInfo.getMaxPointDistanceFromCenter() &gt; maximumDistance) {
		    clusters.add(cluster);

		}
	    }
	}
	return clusters;
    }

}

