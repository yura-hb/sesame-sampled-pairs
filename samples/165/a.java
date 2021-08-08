import org.nd4j.autodiff.samediff.SameDiff;
import org.nd4j.linalg.exception.ND4JIllegalStateException;

abstract class DifferentialFunction {
    /**
     * The left argument for this function
     * @return
     */
    public SDVariable larg() {
	val args = args();
	if (args == null || args.length == 0)
	    throw new ND4JIllegalStateException("No arguments found.");
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

