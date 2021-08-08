import com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import com.sun.org.apache.bcel.internal.generic.INVOKESTATIC;
import com.sun.org.apache.bcel.internal.generic.InstructionList;
import com.sun.org.apache.bcel.internal.generic.PUSH;
import java.util.List;

class UnsupportedElement extends SyntaxTreeNode {
    /**
     * Translate the fallback element (if any).
     */
    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	if (_fallbacks != null) {
	    int count = _fallbacks.size();
	    for (int i = 0; i &lt; count; i++) {
		Fallback fallback = (Fallback) _fallbacks.get(i);
		fallback.translate(classGen, methodGen);
	    }
	}
	// We only go into the else block in forward-compatibility mode, when
	// the unsupported element has no fallback.
	else {
	    // If the unsupported element does not have any fallback child, then
	    // at runtime, a runtime error should be raised when the unsupported
	    // element is instantiated. Otherwise, no error is thrown.
	    ConstantPoolGen cpg = classGen.getConstantPool();
	    InstructionList il = methodGen.getInstructionList();

	    final int unsupportedElem = cpg.addMethodref(BASIS_LIBRARY_CLASS, "unsupported_ElementF",
		    "(" + STRING_SIG + "Z)V");
	    il.append(new PUSH(cpg, getQName().toString()));
	    il.append(new PUSH(cpg, _isExtension));
	    il.append(new INVOKESTATIC(unsupportedElem));
	}
    }

    private List&lt;SyntaxTreeNode&gt; _fallbacks = null;
    private boolean _isExtension = false;

}

