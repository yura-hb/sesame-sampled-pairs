class ComputationGraph implements Serializable, Model, NeuralNetwork {
    /**
     * Set all labels for the ComputationGraph network
     */
    public void setLabels(INDArray... labels) {
	if (labels != null && labels.length != this.numOutputArrays) {
	    throw new IllegalArgumentException("Invalid output array: network has " + numOutputArrays
		    + " outputs, but array is of length " + labels.length);
	}
	this.labels = labels;
    }

    /**
     * The number of output arrays to the network. Many networks only have 1 output; however, a ComputationGraph may
     * have an arbitrary number (&gt;=1) separate output arrays
     */
    private int numOutputArrays;
    private transient INDArray[] labels;

}

