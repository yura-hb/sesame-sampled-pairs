import org.nd4j.autodiff.samediff.SameDiff;

abstract class DifferentialFunction {
    /**
     * Return the first argument
     * @return
     */
    public SDVariable arg() {
	if (args() == null || args().length == 0)
	    return null;
	return args()[0];
    }

    @Getter
    @Setter
    @JsonIgnore
    protected SameDiff sameDiff;

    /**
     * Return the arguments for a given function
     * @return the arguments for a given function
     */
    public SDVariable[] args() {
	return sameDiff.getInputVariablesForFunction(this);
    }

}

