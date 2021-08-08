import java.util.Stack;
import org.eclipse.jdt.internal.compiler.lookup.*;

abstract class Annotation extends Expression {
    /**
     * Return the location for the corresponding annotation inside the type reference, &lt;code&gt;null&lt;/code&gt; if none.
     */
    public static int[] getLocations(final Expression reference, final Annotation annotation) {

	class LocationCollector extends ASTVisitor {
	    Stack typePathEntries;
	    Annotation searchedAnnotation;
	    boolean continueSearch = true;

	    public LocationCollector(Annotation currentAnnotation) {
		this.typePathEntries = new Stack();
		this.searchedAnnotation = currentAnnotation;
	    }

	    private int[] computeNestingDepth(TypeReference typeReference) {
		TypeBinding type = typeReference.resolvedType == null ? null
			: typeReference.resolvedType.leafComponentType();
		int[] nestingDepths = new int[typeReference.getAnnotatableLevels()];
		if (type != null && type.isNestedType()) {
		    int depth = 0;
		    TypeBinding currentType = type;
		    while (currentType != null) {
			depth += (currentType.isStatic()) ? 0 : 1;
			currentType = currentType.enclosingType();
		    }
		    // Work backwards computing whether a INNER_TYPE entry is required for each level
		    int counter = nestingDepths.length - 1;
		    while (type != null && counter &gt;= 0) {
			nestingDepths[counter--] = depth;
			depth -= type.isStatic() ? 0 : 1;
			type = type.enclosingType();
		    }
		}
		return nestingDepths;
	    }

	    private void inspectAnnotations(Annotation[] annotations) {
		for (int i = 0, length = annotations == null ? 0 : annotations.length; this.continueSearch
			&& i &lt; length; i++) {
		    if (annotations[i] == this.searchedAnnotation)
			this.continueSearch = false;
		}
	    }

	    private void inspectArrayDimensions(Annotation[][] annotationsOnDimensions, int dimensions) {
		for (int i = 0; this.continueSearch && i &lt; dimensions; i++) {
		    Annotation[] annotations = annotationsOnDimensions == null ? null : annotationsOnDimensions[i];
		    inspectAnnotations(annotations);
		    if (!this.continueSearch)
			return;
		    this.typePathEntries.push(TYPE_PATH_ELEMENT_ARRAY);
		}
	    }

	    private void inspectTypeArguments(TypeReference[] typeReferences) {
		for (int i = 0, length = typeReferences == null ? 0 : typeReferences.length; this.continueSearch
			&& i &lt; length; i++) {
		    int size = this.typePathEntries.size();
		    this.typePathEntries.add(new int[] { 3, i });
		    typeReferences[i].traverse(this, (BlockScope) null);
		    if (!this.continueSearch)
			return;
		    this.typePathEntries.setSize(size);
		}
	    }

	    public boolean visit(TypeReference typeReference, BlockScope scope) {
		if (this.continueSearch) {
		    inspectArrayDimensions(typeReference.getAnnotationsOnDimensions(), typeReference.dimensions());
		    if (this.continueSearch) {
			int[] nestingDepths = computeNestingDepth(typeReference);
			Annotation[][] annotations = typeReference.annotations;
			TypeReference[][] typeArguments = typeReference.getTypeArguments();
			int levels = typeReference.getAnnotatableLevels();
			int size = this.typePathEntries.size();
			for (int i = levels - 1; this.continueSearch && i &gt;= 0; i--) { // traverse outwards, see comment below about type annotations from SE7 locations.
			    this.typePathEntries.setSize(size);
			    for (int j = 0, depth = nestingDepths[i]; j &lt; depth; j++)
				this.typePathEntries.add(TYPE_PATH_INNER_TYPE);
			    if (annotations != null)
				inspectAnnotations(annotations[i]);
			    if (this.continueSearch && typeArguments != null) {
				inspectTypeArguments(typeArguments[i]);
			    }
			}
		    }
		}
		return false; // if annotation is not found in the type reference, it must be one from SE7 location, typePathEntries captures the proper path entries for them. 
	    }

	    @Override
	    public boolean visit(SingleTypeReference typeReference, BlockScope scope) {
		return visit((TypeReference) typeReference, scope);
	    }

	    @Override
	    public boolean visit(ArrayTypeReference typeReference, BlockScope scope) {
		return visit((TypeReference) typeReference, scope);
	    }

	    @Override
	    public boolean visit(ParameterizedSingleTypeReference typeReference, BlockScope scope) {
		return visit((TypeReference) typeReference, scope);
	    }

	    @Override
	    public boolean visit(QualifiedTypeReference typeReference, BlockScope scope) {
		return visit((TypeReference) typeReference, scope);
	    }

	    @Override
	    public boolean visit(ArrayQualifiedTypeReference typeReference, BlockScope scope) {
		return visit((TypeReference) typeReference, scope);
	    }

	    @Override
	    public boolean visit(ParameterizedQualifiedTypeReference typeReference, BlockScope scope) {
		return visit((TypeReference) typeReference, scope);
	    }

	    @Override
	    public boolean visit(Wildcard typeReference, BlockScope scope) {
		visit((TypeReference) typeReference, scope);
		if (this.continueSearch) {
		    TypeReference bound = typeReference.bound;
		    if (bound != null) {
			int size = this.typePathEntries.size();
			this.typePathEntries.push(TYPE_PATH_ANNOTATION_ON_WILDCARD_BOUND);
			bound.traverse(this, scope);
			if (this.continueSearch)
			    this.typePathEntries.setSize(size);
		    }
		}
		return false;
	    }

	    @Override
	    public boolean visit(ArrayAllocationExpression allocationExpression, BlockScope scope) {
		if (this.continueSearch) {
		    inspectArrayDimensions(allocationExpression.getAnnotationsOnDimensions(),
			    allocationExpression.dimensions.length);
		    if (this.continueSearch) {
			allocationExpression.type.traverse(this, scope);
		    }
		    if (this.continueSearch)
			throw new IllegalStateException();
		}
		return false;
	    }

	    @Override
	    public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("search location for ") //$NON-NLS-1$
			.append(this.searchedAnnotation).append("\ncurrent type_path entries : "); //$NON-NLS-1$
		for (int i = 0, maxi = this.typePathEntries.size(); i &lt; maxi; i++) {
		    int[] typePathEntry = (int[]) this.typePathEntries.get(i);
		    buffer.append('(').append(typePathEntry[0]).append(',').append(typePathEntry[1]).append(')');
		}
		return String.valueOf(buffer);
	    }
	}
	if (reference == null)
	    return null;
	LocationCollector collector = new LocationCollector(annotation);
	reference.traverse(collector, (BlockScope) null);
	if (collector.typePathEntries.isEmpty()) {
	    return null;
	}
	int size = collector.typePathEntries.size();
	int[] result = new int[size * 2];
	int offset = 0;
	for (int i = 0; i &lt; size; i++) {
	    int[] pathElement = (int[]) collector.typePathEntries.get(i);
	    result[offset++] = pathElement[0];
	    result[offset++] = pathElement[1];
	}
	return result;
    }

    static final int[] TYPE_PATH_ELEMENT_ARRAY = new int[] { 0, 0 };
    static final int[] TYPE_PATH_INNER_TYPE = new int[] { 1, 0 };
    static final int[] TYPE_PATH_ANNOTATION_ON_WILDCARD_BOUND = new int[] { 2, 0 };

    class LocationCollector extends ASTVisitor {
	static final int[] TYPE_PATH_ELEMENT_ARRAY = new int[] { 0, 0 };
	static final int[] TYPE_PATH_INNER_TYPE = new int[] { 1, 0 };
	static final int[] TYPE_PATH_ANNOTATION_ON_WILDCARD_BOUND = new int[] { 2, 0 };

	private void inspectAnnotations(Annotation[] annotations) {
	    for (int i = 0, length = annotations == null ? 0 : annotations.length; this.continueSearch
		    && i &lt; length; i++) {
		if (annotations[i] == this.searchedAnnotation)
		    this.continueSearch = false;
	    }
	}

	private void inspectArrayDimensions(Annotation[][] annotationsOnDimensions, int dimensions) {
	    for (int i = 0; this.continueSearch && i &lt; dimensions; i++) {
		Annotation[] annotations = annotationsOnDimensions == null ? null : annotationsOnDimensions[i];
		inspectAnnotations(annotations);
		if (!this.continueSearch)
		    return;
		this.typePathEntries.push(TYPE_PATH_ELEMENT_ARRAY);
	    }
	}

	private int[] computeNestingDepth(TypeReference typeReference) {
	    TypeBinding type = typeReference.resolvedType == null ? null
		    : typeReference.resolvedType.leafComponentType();
	    int[] nestingDepths = new int[typeReference.getAnnotatableLevels()];
	    if (type != null && type.isNestedType()) {
		int depth = 0;
		TypeBinding currentType = type;
		while (currentType != null) {
		    depth += (currentType.isStatic()) ? 0 : 1;
		    currentType = currentType.enclosingType();
		}
		// Work backwards computing whether a INNER_TYPE entry is required for each level
		int counter = nestingDepths.length - 1;
		while (type != null && counter &gt;= 0) {
		    nestingDepths[counter--] = depth;
		    depth -= type.isStatic() ? 0 : 1;
		    type = type.enclosingType();
		}
	    }
	    return nestingDepths;
	}

	private void inspectTypeArguments(TypeReference[] typeReferences) {
	    for (int i = 0, length = typeReferences == null ? 0 : typeReferences.length; this.continueSearch
		    && i &lt; length; i++) {
		int size = this.typePathEntries.size();
		this.typePathEntries.add(new int[] { 3, i });
		typeReferences[i].traverse(this, (BlockScope) null);
		if (!this.continueSearch)
		    return;
		this.typePathEntries.setSize(size);
	    }
	}

	public boolean visit(TypeReference typeReference, BlockScope scope) {
	    if (this.continueSearch) {
		inspectArrayDimensions(typeReference.getAnnotationsOnDimensions(), typeReference.dimensions());
		if (this.continueSearch) {
		    int[] nestingDepths = computeNestingDepth(typeReference);
		    Annotation[][] annotations = typeReference.annotations;
		    TypeReference[][] typeArguments = typeReference.getTypeArguments();
		    int levels = typeReference.getAnnotatableLevels();
		    int size = this.typePathEntries.size();
		    for (int i = levels - 1; this.continueSearch && i &gt;= 0; i--) { // traverse outwards, see comment below about type annotations from SE7 locations.
			this.typePathEntries.setSize(size);
			for (int j = 0, depth = nestingDepths[i]; j &lt; depth; j++)
			    this.typePathEntries.add(TYPE_PATH_INNER_TYPE);
			if (annotations != null)
			    inspectAnnotations(annotations[i]);
			if (this.continueSearch && typeArguments != null) {
			    inspectTypeArguments(typeArguments[i]);
			}
		    }
		}
	    }
	    return false; // if annotation is not found in the type reference, it must be one from SE7 location, typePathEntries captures the proper path entries for them. 
	}

	public LocationCollector(Annotation currentAnnotation) {
	    this.typePathEntries = new Stack();
	    this.searchedAnnotation = currentAnnotation;
	}

    }

}

