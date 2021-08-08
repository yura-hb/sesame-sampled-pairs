class BaseSparseNDArrayCOO extends BaseSparseNDArray {
    /**
     * Return if the dimension in argument is a fixed dimension.
     * */
    public boolean isDimensionFixed(int i) {
	return flags()[i] == 1;
    }

}

