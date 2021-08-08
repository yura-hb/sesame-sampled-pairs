import org.nd4j.base.Preconditions;
import java.util.*;

class SameDiff {
    /**
     * Add the specified variable to this SameDiff instance
     * @param variable Variable to add
     */
    public void addVariable(SDVariable variable) {
	if (variableMap == null)
	    variableMap = new HashMap&lt;&gt;();

	Preconditions.checkState(variable.getSameDiff() == this, "Samediff instance must be the same.");

	/**
	 * Of note here:
	 * We don't validate based on vertex id because more than one input can have the same
	 * vertex id as a result.
	 *
	 * We validate based on variable opName instead which takes in to account function names as well
	 * as input ids
	 */
	if (variableMap.containsKey(variable.getVarName())
		&& !variableMap.get(variable.getVarName()).equals(variable)) {
	    throw new IllegalArgumentException("Variable already found with variable opName " + variable.getVarName());
	}

	Preconditions.checkState(variable.getSameDiff() == this, "Same diff instance for variable must be the same!");
	variableMap.put(variable.getVarName(), variable);
    }

    private Map&lt;String, SDVariable&gt; variableMap;

}

