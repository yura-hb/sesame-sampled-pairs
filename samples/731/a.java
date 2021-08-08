import org.nd4j.linalg.factory.Nd4j;

class BaseSparseNDArrayCOO extends BaseSparseNDArray {
    /**
     * Sort the indexes and the values buffers
     * */
    public void sort() {
	if (!isSorted) {
	    Nd4j.sparseFactory().sortCooIndices(this);
	    isSorted = true;
	}
    }

    protected transient volatile boolean isSorted = false;

}

