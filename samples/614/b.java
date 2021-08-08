import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.shape.Shape;
import org.nd4j.linalg.factory.Nd4j;

class BaseSparseNDArrayCOO extends BaseSparseNDArray {
    /**
     * Returns the indices of non-zero element of the vector
     *
     * @return indices in Databuffer
     * */
    @Override
    public DataBuffer getVectorCoordinates() {
	int idx;
	if (isRowVector()) {
	    idx = 1;
	} else if (isColumnVector()) {
	    idx = 0;
	} else {
	    throw new UnsupportedOperationException();
	}

	// FIXME: int cast
	int[] temp = new int[(int) length()];
	for (int i = 0; i &lt; length(); i++) {
	    temp[i] = getUnderlyingIndicesOf(i).getInt(idx);
	}
	return Nd4j.createBuffer(temp);
    }

    protected transient volatile DataBuffer indices;

    /**
     * Returns the underlying indices of the element of the given index
     * such as there really are in the original ndarray
     *
     * @param i the index of the element+
     * @return a dataBuffer containing the indices of element
     * */
    public DataBuffer getUnderlyingIndicesOf(int i) {
	int from = underlyingRank() * i;
	//int to = from + underlyingRank();
	int[] res = new int[underlyingRank()];
	for (int j = 0; j &lt; underlyingRank(); j++) {
	    res[j] = indices.getInt(from + j);
	}

	///int[] arr = Arrays.copyOfRange(indices.asInt(), from, to);
	return Nd4j.getDataBufferFactory().createInt(res);
    }

    @Override
    public int underlyingRank() {
	return Shape.underlyingRank(sparseInformation);
    }

}

