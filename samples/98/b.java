import org.nd4j.linalg.api.ops.impl.accum.distances.EuclideanDistance;
import org.nd4j.linalg.factory.Nd4j;
import static org.nd4j.linalg.factory.Nd4j.*;

abstract class BaseNDArray implements INDArray, Iterable {
    /**
     * Returns the square of the Euclidean distance.
     */
    @Override
    public double squaredDistance(INDArray other) {
	double d2 = distance2(other);
	return d2 * d2;
    }

    /**
     * Returns the (euclidean) distance.
     */
    @Override
    public double distance2(INDArray other) {
	Nd4j.getCompressor().autoDecompress(this);
	return Nd4j.getExecutioner().execAndReturn(new EuclideanDistance(this, other)).getFinalResult().doubleValue();
    }

}

