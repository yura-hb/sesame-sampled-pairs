import org.nd4j.linalg.api.ops.impl.transforms.*;

class DifferentialFunctionFactory {
    /**
     * Returns a boolean mask of equal shape to the input, where the condition is satisfied
     *
     * @param in        Input
     * @param condition Condition
     * @return Boolean mask
     */
    public SDVariable matchCondition(SDVariable in, Condition condition) {
	return new MatchConditionTransform(sameDiff(), in, condition).outputVariable();
    }

    protected SameDiff sameDiff;

    public SameDiff sameDiff() {
	return sameDiff;
    }

}

