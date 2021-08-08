import org.eclipse.jdt.internal.compiler.codegen.AnnotationTargetTypeConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;

class TypeAnnotationWalker implements ITypeAnnotationWalker {
    /**
     * {@inheritDoc}
     * &lt;p&gt;(superTypesSignature is ignored in this implementation).&lt;/p&gt;
     */
    @Override
    public ITypeAnnotationWalker toSupertype(short index, char[] superTypeSignature) {
	long newMatches = this.matches;
	if (newMatches == 0)
	    return EMPTY_ANNOTATION_WALKER;
	int length = this.typeAnnotations.length;
	long mask = 1;
	for (int i = 0; i &lt; length; i++, mask = mask &lt;&lt; 1) {
	    IBinaryTypeAnnotation candidate = this.typeAnnotations[i];
	    if (candidate.getTargetType() != AnnotationTargetTypeConstants.CLASS_EXTENDS
		    || (short) candidate.getSupertypeIndex() != index) {
		newMatches &= ~mask;
	    }
	}
	return restrict(newMatches, 0);
    }

    final protected long matches;
    final protected IBinaryTypeAnnotation[] typeAnnotations;
    final protected int pathPtr;

    protected ITypeAnnotationWalker restrict(long newMatches, int newPathPtr) {
	if (this.matches == newMatches && this.pathPtr == newPathPtr)
	    return this;
	if (newMatches == 0 || this.typeAnnotations == null || this.typeAnnotations.length == 0)
	    return EMPTY_ANNOTATION_WALKER;
	return new TypeAnnotationWalker(this.typeAnnotations, newMatches, newPathPtr);
    }

    protected TypeAnnotationWalker(IBinaryTypeAnnotation[] typeAnnotations, long matchBits, int pathPtr) {
	this.typeAnnotations = typeAnnotations;
	this.matches = matchBits;
	this.pathPtr = pathPtr;
    }

}

