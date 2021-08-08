import org.nd4j.linalg.exception.ND4JIllegalStateException;
import java.util.*;

class SameDiff {
    /**
     * Add a property for the given function
     *
     * @param functionFor  the function add a property for
     * @param propertyName the property name
     * @param property     the property value
     */
    public void addPropertyForFunction(DifferentialFunction functionFor, String propertyName, INDArray property) {
	addPropertyForFunction(functionFor, propertyName, (Object) property);
    }

    /**
     * A map of own name to
     * the properties of the function (things like execution axes etc)
     * The valid values can be:
     * int
     * long
     * INDArray
     */
    private Map&lt;String, Map&lt;String, Object&gt;&gt; propertiesForFunction;

    private void addPropertyForFunction(DifferentialFunction functionFor, String propertyName, Object propertyValue) {
	if (!propertiesForFunction.containsKey(functionFor.getOwnName())) {
	    Map&lt;String, Object&gt; fields = new LinkedHashMap&lt;&gt;();
	    fields.put(propertyName, propertyValue);
	    propertiesForFunction.put(functionFor.getOwnName(), fields);
	} else {
	    val fieldMap = propertiesForFunction.get(functionFor.getOwnName());
	    if (fieldMap.containsKey(propertyName)) {
		throw new ND4JIllegalStateException("Attempting to override property " + propertyName);
	    }

	    fieldMap.put(propertyName, propertyValue);
	}
    }

}

