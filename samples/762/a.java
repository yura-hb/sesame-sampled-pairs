import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.*;

class Initializer extends FieldDeclaration {
    /** Method used only by DOM to support bindings of initializers. */
    public MethodBinding getMethodBinding() {
	if (this.methodBinding == null) {
	    Scope scope = this.block.scope;
	    this.methodBinding = isStatic()
		    ? new MethodBinding(ClassFileConstants.AccStatic, CharOperation.NO_CHAR, TypeBinding.VOID,
			    Binding.NO_PARAMETERS, Binding.NO_EXCEPTIONS, scope.enclosingSourceType())
		    : new MethodBinding(0, CharOperation.NO_CHAR, TypeBinding.VOID, Binding.NO_PARAMETERS,
			    Binding.NO_EXCEPTIONS, scope.enclosingSourceType());
	}
	return this.methodBinding;
    }

    private MethodBinding methodBinding;
    public Block block;

    @Override
    public boolean isStatic() {

	return (this.modifiers & ClassFileConstants.AccStatic) != 0;
    }

}

