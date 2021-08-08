import org.apache.commons.math4.ml.neuralnet.MapUtils;
import org.apache.commons.math4.ml.neuralnet.twod.NeuronSquareMesh2D;
import org.apache.commons.math4.geometry.euclidean.threed.Cartesian3D;
import org.apache.commons.math4.exception.MathUnsupportedOperationException;

class ChineseRingsClassifier {
    /**
     * Computes the topographic error.
     *
     * @return the topographic error.
     */
    public double computeTopographicError() {
	return MapUtils.computeTopographicError(createIterable(), sofm.getNetwork(), distance);
    }

    /** SOFM. */
    private final NeuronSquareMesh2D sofm;
    /** Distance function. */
    private final DistanceMeasure distance = new EuclideanDistance();
    /** Rings. */
    private final ChineseRings rings;

    /**
     * Creates an iterable that will present the points coordinates.
     *
     * @return the iterable.
     */
    private Iterable&lt;double[]&gt; createIterable() {
	return new Iterable&lt;double[]&gt;() {
	    public Iterator&lt;double[]&gt; iterator() {
		return new Iterator&lt;double[]&gt;() {
		    /** Data. */
		    final Cartesian3D[] points = rings.getPoints();
		    /** Number of samples. */
		    private int n = 0;

		    /** {@inheritDoc} */
		    public boolean hasNext() {
			return n &lt; points.length;
		    }

		    /** {@inheritDoc} */
		    public double[] next() {
			return points[n++].toArray();
		    }

		    /** {@inheritDoc} */
		    public void remove() {
			throw new MathUnsupportedOperationException();
		    }
		};
	    }
	};
    }

}

