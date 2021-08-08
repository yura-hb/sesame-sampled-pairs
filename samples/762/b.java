import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.code.Type;

class ClassDocImpl extends ProgramElementDocImpl implements ClassDoc {
    /**
     * Determine if a class is a RuntimeException.
     * &lt;p&gt;
     * Used only by ThrowsTagImpl.
     */
    boolean isRuntimeException() {
	return tsym.isSubClass(env.syms.runtimeExceptionType.tsym, env.types);
    }

    public final ClassSymbol tsym;

}

