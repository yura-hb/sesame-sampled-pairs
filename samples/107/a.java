import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

class QualifiedNameReference extends NameReference {
    /**
    * Normal field binding did not work, try to bind to a field of the delegate receiver.
    */
    public TypeBinding reportError(BlockScope scope) {
	if (this.binding instanceof ProblemFieldBinding) {
	    scope.problemReporter().invalidField(this, (FieldBinding) this.binding);
	} else if (this.binding instanceof ProblemReferenceBinding || this.binding instanceof MissingTypeBinding) {
	    scope.problemReporter().invalidType(this, (TypeBinding) this.binding);
	} else {
	    scope.problemReporter().unresolvableReference(this, this.binding);
	}
	return null;
    }

}

