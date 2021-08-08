import org.nd4j.linalg.api.ops.impl.accum.*;
import org.nd4j.linalg.factory.Nd4j;
import static org.nd4j.linalg.factory.Nd4j.*;

abstract class BaseNDArray implements INDArray, Iterable {
    /**
     * Standard deviation of an ndarray along a dimension
     *
     * @param dimension the dimension to getScalar the std along
     * @return the standard deviation along a particular dimension
     */
    @Override
    public INDArray std(int... dimension) {
	return Nd4j.getExecutioner().exec(new StandardDeviation(this), dimension);
    }

}

