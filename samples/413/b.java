import java.util.ArrayList;
import java.util.List;

class FunctionCall extends Expression {
    /**
     * Type check the actual arguments of this function call.
     */
    public List&lt;Type&gt; typeCheckArgs(SymbolTable stable) throws TypeCheckError {
	final List&lt;Type&gt; result = new ArrayList&lt;&gt;();
	for (Expression exp : _arguments) {
	    result.add(exp.typeCheck(stable));
	}
	return result;
    }

    private final List&lt;Expression&gt; _arguments;

}

