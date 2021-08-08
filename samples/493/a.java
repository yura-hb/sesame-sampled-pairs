class ComputationGraph implements Serializable, Model, NeuralNetwork {
    /**
     * Set the specified input for the ComputationGraph
     */
    public void setInput(int inputNum, INDArray input) {
	if (inputs == null) {
	    //May be null after clear()
	    inputs = new INDArray[numInputArrays];
	}
	inputs[inputNum] = input;
    }

    private transient INDArray[] inputs;
    /**
     * The number of input arrays to the network. Many networks only have 1 input; however, a ComputationGraph may
     * have an arbitrary number (&gt;=1) separate input arrays
     */
    private int numInputArrays;

}

