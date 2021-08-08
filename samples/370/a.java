import org.nd4j.linalg.api.ops.impl.transforms.Negative;
import org.nd4j.linalg.factory.Nd4j;
import static org.nd4j.linalg.factory.Nd4j.*;

abstract class BaseNDArray implements INDArray, Iterable {
    /**
     * Negate each element (in-place).
     */
    @Override
    public INDArray negi() {
	Nd4j.getExecutioner().exec(new Negative(this));
	return this;
    }

}

