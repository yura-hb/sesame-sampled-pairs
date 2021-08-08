import org.nd4j.autodiff.functions.DifferentialFunction;
import org.nd4j.linalg.api.ndarray.INDArray;
import java.util.*;

class SameDiff {
    /**
     * Print the given function for debugging (will not print functions)
     *
     * @param differentialFunction the function to print
     */
    public void printFunction(DifferentialFunction differentialFunction) {
	if (!logExecution)
	    return;
	if (differentialFunction instanceof SDVariable)
	    return;

	StringBuilder argShapes = new StringBuilder();
	for (val arg : differentialFunction.args()) {
	    argShapes.append(" Variable " + arg.getVarName() + " Shape for " + Arrays.toString(arg.getShape()));
	}

	for (val func : differentialFunction.outputVariables()) {
	    argShapes.append("  Output variable " + func.getVarName() + " is " + Arrays.toString(func.getShape()));
	}

	StringBuilder realShapes = new StringBuilder();
	for (val arg : differentialFunction.args()) {
	    realShapes.append(" Input shape for " + arg.getVarName() + " is  "
		    + Arrays.toString(getShapeForVarName(arg.getVarName())));
	}

	for (val arg : differentialFunction.outputVariables()) {
	    realShapes.append(" Output shape for " + arg.getVarName() + " is  "
		    + Arrays.toString(getShapeForVarName(arg.getVarName())));
	}

	//        log.info(realShapes.toString());
    }

    @Getter
    @Setter
    boolean logExecution = true;
    private Map&lt;String, INDArray&gt; variableNameToArr;
    private Map&lt;String, long[]&gt; variableNameToShape;

    /**
     * Get the shape for the given vertex id.
     * Note that if an array is defined, it will use the shape of the array instead.
     * &lt;p&gt;
     * A shape *and* an array should not be defined at the same time.
     * This wastes memory. The internal map used for tracking shapes for particular
     * vertex ids should also delete redundant shapes stored to avoid redundant sources of information.
     *
     * @param varName the vertex id to get the shape for
     * @return the shape for the given vertex if if any.
     */
    public long[] getShapeForVarName(String varName) {
	if (variableNameToArr.containsKey(varName)) {
	    return variableNameToArr.get(varName).shape();
	}
	return variableNameToShape.get(varName);
    }

}

