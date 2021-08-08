abstract class DifferentialFunction {
    /**
     * The opName of this function tensorflow
     *
     * @return
     */
    public String[] tensorflowNames() {
	return new String[] { tensorflowName() };
    }

    /**
     * The opName of this function tensorflow
     *
     * @return
     */
    public abstract String tensorflowName();

}

