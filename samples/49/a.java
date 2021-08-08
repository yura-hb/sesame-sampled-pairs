import java.util.ArrayList;

class StraightLineProblem {
    /**
     * @return the list of y-coordinates.
     */
    public double[] y() {
	final double[] v = new double[points.size()];
	for (int i = 0; i &lt; points.size(); i++) {
	    final double[] p = points.get(i);
	    v[i] = p[1]; // y-coordinate.
	}

	return v;
    }

    /** Cloud of points assumed to be fitted by a straight line. */
    private final ArrayList&lt;double[]&gt; points;

}

