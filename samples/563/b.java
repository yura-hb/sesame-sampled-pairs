import org.nd4j.autodiff.functions.DifferentialFunction;

class SDVariable extends DifferentialFunction implements Serializable {
    /**
     *
     * @param value
     * @return
     */
    public SDVariable mul(String varName, double value) {
	val function = sameDiff.f().mul(this, value);
	return sameDiff.updateVariableNameAndReference(function, varName);
    }

}

