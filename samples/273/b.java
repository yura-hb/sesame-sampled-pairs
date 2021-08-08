import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.util.Name;

class ClassDocImpl extends ProgramElementDocImpl implements ClassDoc {
    /**
     * Return the simple name of this type.
     */
    public String simpleTypeName() {
	if (simpleTypeName == null) {
	    simpleTypeName = tsym.name.toString();
	}
	return simpleTypeName;
    }

    private String simpleTypeName;
    public final ClassSymbol tsym;

}

