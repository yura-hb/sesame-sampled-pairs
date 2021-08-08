import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.util.ArrayUtil;

class BaseSparseNDArrayCOO extends BaseSparseNDArray {
    /**
     * Create a DataBuffer for indices of given arrays of indices.
     * @param indices
     * @param shape
     * @return
     */
    protected static DataBuffer createIndiceBuffer(long[][] indices, long[] shape) {
	checkNotNull(indices);
	checkNotNull(shape);
	if (indices.length == 0) {
	    return Nd4j.getDataBufferFactory().createLong(shape.length);
	}

	if (indices.length == shape.length) {
	    return Nd4j.createBuffer(ArrayUtil.flattenF(indices));
	}

	return Nd4j.createBuffer(ArrayUtil.flatten(indices));
    }

}

