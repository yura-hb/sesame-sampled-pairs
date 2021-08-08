abstract class TypeReference extends Expression {
    /**
    * info can be either a type index (superclass/superinterfaces) or a pc into the bytecode
    * @param targetType
    * @param info
    * @param allAnnotationContexts
    */
    public void getAllAnnotationContexts(int targetType, int info, List allAnnotationContexts) {
	AnnotationCollector collector = new AnnotationCollector(this, targetType, info, allAnnotationContexts);
	this.traverse(collector, (BlockScope) null);
    }

    @Override
    public abstract void traverse(ASTVisitor visitor, BlockScope scope);

    class AnnotationCollector extends ASTVisitor {
	public AnnotationCollector(Expression typeReference, int targetType, int info, List annotationContexts) {
	    this.annotationContexts = annotationContexts;
	    this.typeReference = typeReference;
	    this.info = info;
	    this.targetType = targetType;
	}

    }

}

