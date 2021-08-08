import org.nd4j.linalg.factory.Nd4j;
import java.util.*;

class OpValidation {
    /**
     * Validate the outputs of a single op
     *
     * @param testCase Op test case to run
     * @return NULL if test is OK, or an error message otherwise
     */
    public static String validate(OpTestCase testCase) {
	collectCoverageInformation(testCase);

	//Check shape function:
	List&lt;long[]&gt; outShapes;
	try {
	    outShapes = Nd4j.getExecutioner().calculateOutputShape(testCase.op());
	} catch (Throwable t) {
	    throw new IllegalStateException("Error calculating output shapes during op validation", t);
	}

	if (outShapes.size() != testCase.testFns().size()) {
	    return "Expected number of output shapes and number of outputs differ. " + outShapes.size()
		    + " output shapes," + " but OpTestCase specifies " + testCase.testFns().size()
		    + " outputs expected";
	}

	for (int i = 0; i &lt; outShapes.size(); i++) {
	    long[] act = outShapes.get(i);
	    long[] exp = testCase.expShapes().get(i);
	    if (!Arrays.equals(exp, act)) {
		return "Shape function check failed for output " + i + ": expected shape " + Arrays.toString(exp)
			+ ", actual shape " + Arrays.toString(act);
	    }
	}

	//Check the outputs:
	try {
	    Nd4j.getExecutioner().exec(testCase.op());
	} catch (Throwable t) {
	    throw new IllegalStateException("Error during op execution", t);
	}

	for (int i = 0; i &lt; testCase.testFns().size(); i++) {
	    String error;
	    try {
		error = testCase.testFns().get(i).apply(testCase.op().outputArguments()[i]);
	    } catch (Throwable t) {
		throw new IllegalStateException("Exception thrown during op output validation for output " + i, t);
	    }

	    if (error != null) {
		return "Output " + i + " failed: " + error;
	    }
	}

	return null; //OK
    }

    private static Map&lt;Class, Integer&gt; singleOpTestCountPerClass = new LinkedHashMap&lt;&gt;();

    private static void collectCoverageInformation(OpTestCase testCase) {
	//TODO we're basically assuming subtypes of DynamicCustomOp here, for coverage... not DCO itself
	singleOpTestCountPerClass.put(testCase.op().getClass(),
		singleOpTestCountPerClass.get(testCase.op().getClass()) + 1);
    }

}

