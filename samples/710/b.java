import java.util.ArrayList;
import java.util.List;

abstract class SequenceElement implements Comparable&lt;SequenceElement&gt;, Serializable {
    /**
     * Sets Huffman tree points
     *
     * @param points
     */
    @JsonIgnore
    public void setPoints(int[] points) {
	this.points = new ArrayList&lt;&gt;();
	for (int i = 0; i &lt; points.length; i++) {
	    this.points.add(points[i]);
	}
    }

    protected List&lt;Integer&gt; points = new ArrayList&lt;&gt;();

}

