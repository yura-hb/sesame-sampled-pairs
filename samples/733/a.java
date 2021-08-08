import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math4.geometry.partitioning.AbstractRegion;

class ArcsSet extends AbstractRegion&lt;Sphere1D, Sphere1D&gt; implements Iterable&lt;double[]&gt; {
    /** Build an ordered list of arcs representing the instance.
     * &lt;p&gt;This method builds this arcs set as an ordered list of
     * {@link Arc Arc} elements. An empty tree will build an empty list
     * while a tree representing the whole circle will build a one
     * element list with bounds set to \( 0 and 2 \pi \).&lt;/p&gt;
     * @return a new ordered list containing {@link Arc Arc} elements
     */
    public List&lt;Arc&gt; asList() {
	final List&lt;Arc&gt; list = new ArrayList&lt;&gt;();
	for (final double[] a : this) {
	    list.add(new Arc(a[0], a[1], getTolerance()));
	}
	return list;
    }

}

