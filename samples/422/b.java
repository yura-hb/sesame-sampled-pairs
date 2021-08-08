import org.nd4j.linalg.api.ops.impl.accum.distances.ManhattanDistance;
import org.nd4j.linalg.factory.Nd4j;
import static org.nd4j.linalg.factory.Nd4j.*;

abstract class BaseNDArray implements INDArray, Iterable {
    /**
     * Returns the (1-norm) distance.
     */
    @Override
    public double distance1(INDArray other) {
	Nd4j.getCompressor().autoDecompress(this);
	return Nd4j.getExecutioner().execAndReturn(new ManhattanDistance(this, other)).getFinalResult().doubleValue();
    }

}

