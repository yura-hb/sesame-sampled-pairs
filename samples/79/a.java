import org.eclipse.jdt.internal.compiler.ast.*;

abstract class Scope
	implements BaseTypes, BindingIds, CompilerModifiers, ProblemReasons, TagBits, TypeConstants, TypeIds {
    /**
     * Returns the modifiers of the innermost enclosing declaration.
     * @return modifiers
     */
    public int getDeclarationModifiers() {
	switch (this.kind) {
	case Scope.BLOCK_SCOPE:
	case Scope.METHOD_SCOPE:
	    MethodScope methodScope = methodScope();
	    if (!methodScope.isInsideInitializer()) {
		// check method modifiers to see if deprecated
		MethodBinding context = ((AbstractMethodDeclaration) methodScope.referenceContext).binding;
		if (context != null) {
		    return context.modifiers;
		}
	    } else {
		SourceTypeBinding type = ((BlockScope) this).referenceType().binding;

		// inside field declaration ? check field modifier to see if deprecated
		if (methodScope.initializedField != null) {
		    return methodScope.initializedField.modifiers;
		}
		if (type != null) {
		    return type.modifiers;
		}
	    }
	    break;
	case Scope.CLASS_SCOPE:
	    ReferenceBinding context = ((ClassScope) this).referenceType().binding;
	    if (context != null) {
		return context.modifiers;
	    }
	    break;
	}
	return -1;
    }

    public int kind;
    public final static int BLOCK_SCOPE = 1;
    public final static int METHOD_SCOPE = 2;
    public final static int CLASS_SCOPE = 3;
    public Scope parent;

    public final MethodScope methodScope() {
	Scope scope = this;
	do {
	    if (scope instanceof MethodScope)
		return (MethodScope) scope;
	    scope = scope.parent;
	} while (scope != null);
	return null;
    }

}

