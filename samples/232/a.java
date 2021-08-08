import java.util.List;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldList;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;

class NdMethod extends NdBinding {
    /**
     * Returns method parameter names that were not defined by the compiler.
     */
    public char[][] getParameterNames() {
	List&lt;NdMethodParameter&gt; params = getMethodParameters();

	// Use index to count the "real" parameters.
	int index = 0;
	char[][] result = new char[params.size()][];
	for (int idx = 0; idx &lt; result.length; idx++) {
	    NdMethodParameter param = params.get(idx);
	    if (!param.isCompilerDefined()) {
		result[index] = param.getName().getChars();
		index++;
	    }
	}
	return CharArrayUtils.subarray(result, 0, index);
    }

    public static final FieldList&lt;NdMethodParameter&gt; PARAMETERS;

    public List&lt;NdMethodParameter&gt; getMethodParameters() {
	return PARAMETERS.asList(getNd(), this.address);
    }

}

