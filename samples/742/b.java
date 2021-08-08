class Nd4j {
    /**
     * Sort an ndarray along a particular dimension&lt;br&gt;
     * Note that the input array is modified in-place.
     *
     * @param ndarray   the ndarray to sort
     * @param dimension the dimension to sort
     * @return the sorted ndarray
     */
    public static INDArray sort(INDArray ndarray, int dimension, boolean ascending) {
	return getNDArrayFactory().sort(ndarray, !ascending, dimension);
    }

    protected static NDArrayFactory INSTANCE;

    /**
     *
     * @return
     */
    public static NDArrayFactory getNDArrayFactory() {
	return INSTANCE;
    }

}

