import java.util.HashMap;
import java.util.HashSet;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObjectToInt;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.util.Util;

class HandleFactory {
    /**
     * Returns a handle denoting the class member identified by its scope.
     */
    public IJavaElement createElement(ClassScope scope, ICompilationUnit unit, HashSet existingElements,
	    HashMap knownScopes) {
	return createElement(scope, scope.referenceContext.sourceStart, unit, existingElements, knownScopes);
    }

    private HashtableOfObjectToInt localOccurrenceCounts = new HashtableOfObjectToInt(5);

    /**
     * Create handle by adding child to parent obtained by recursing into parent scopes.
     */
    public IJavaElement createElement(Scope scope, int elementPosition, ICompilationUnit unit, HashSet existingElements,
	    HashMap knownScopes) {
	IJavaElement newElement = (IJavaElement) knownScopes.get(scope);
	if (newElement != null)
	    return newElement;

	switch (scope.kind) {
	case Scope.COMPILATION_UNIT_SCOPE:
	    newElement = unit;
	    break;
	case Scope.CLASS_SCOPE:
	    IJavaElement parentElement = createElement(scope.parent, elementPosition, unit, existingElements,
		    knownScopes);
	    switch (parentElement.getElementType()) {
	    case IJavaElement.COMPILATION_UNIT:
		newElement = ((ICompilationUnit) parentElement)
			.getType(new String(scope.enclosingSourceType().sourceName));
		break;
	    case IJavaElement.TYPE:
		newElement = ((IType) parentElement).getType(new String(scope.enclosingSourceType().sourceName));
		break;
	    case IJavaElement.FIELD:
	    case IJavaElement.INITIALIZER:
	    case IJavaElement.METHOD:
		IMember member = (IMember) parentElement;
		if (member.isBinary()) {
		    return null;
		} else {
		    newElement = member.getType(new String(scope.enclosingSourceType().sourceName), 1);
		    // increment occurrence count if collision is detected
		    if (newElement != null) {
			while (!existingElements.add(newElement))
			    ((SourceRefElement) newElement).occurrenceCount++;
		    }
		}
		break;
	    }
	    if (newElement != null) {
		knownScopes.put(scope, newElement);
	    }
	    break;
	case Scope.METHOD_SCOPE:
	    if (scope.isLambdaScope()) {
		parentElement = createElement(scope.parent, elementPosition, unit, existingElements, knownScopes);
		LambdaExpression expression = (LambdaExpression) scope.originalReferenceContext();
		if (expression.resolvedType != null && expression.resolvedType.isValidBinding()
			&& !(expression.descriptor instanceof ProblemMethodBinding)) { // chain in lambda element only if resolved properly.
		    //newElement = new org.eclipse.jdt.internal.core.SourceLambdaExpression((JavaElement) parentElement, expression).getMethod();
		    newElement = LambdaFactory.createLambdaExpression((JavaElement) parentElement, expression)
			    .getMethod();
		    knownScopes.put(scope, newElement);
		    return newElement;
		}
		return parentElement;
	    }
	    IType parentType = (IType) createElement(scope.parent, elementPosition, unit, existingElements,
		    knownScopes);
	    MethodScope methodScope = (MethodScope) scope;
	    if (methodScope.isInsideInitializer()) {
		// inside field or initializer, must find proper one
		TypeDeclaration type = methodScope.referenceType();
		int occurenceCount = 1;
		int length = type.fields == null ? 0 : type.fields.length;
		for (int i = 0; i &lt; length; i++) {
		    FieldDeclaration field = type.fields[i];
		    if (field.declarationSourceStart &lt;= elementPosition
			    && elementPosition &lt;= field.declarationSourceEnd) {
			switch (field.getKind()) {
			case AbstractVariableDeclaration.FIELD:
			case AbstractVariableDeclaration.ENUM_CONSTANT:
			    newElement = parentType.getField(new String(field.name));
			    break;
			case AbstractVariableDeclaration.INITIALIZER:
			    newElement = parentType.getInitializer(occurenceCount);
			    break;
			}
			break;
		    } else if (field.getKind() == AbstractVariableDeclaration.INITIALIZER) {
			occurenceCount++;
		    }
		}
	    } else {
		// method element
		AbstractMethodDeclaration method = methodScope.referenceMethod();
		newElement = parentType.getMethod(new String(method.selector), Util.typeParameterSignatures(method));
		if (newElement != null) {
		    knownScopes.put(scope, newElement);
		}
	    }
	    break;
	case Scope.BLOCK_SCOPE:
	    // standard block, no element per se
	    newElement = createElement(scope.parent, elementPosition, unit, existingElements, knownScopes);
	    break;
	}
	resolveDuplicates(newElement);
	return newElement;
    }

    protected void resolveDuplicates(IJavaElement handle) {

	// For anonymous source types, the occurrence count should be in the context
	// of the enclosing type.
	if (handle instanceof SourceType && ((SourceType) handle).isAnonymous()) {
	    Object key = handle.getParent().getAncestor(IJavaElement.TYPE);
	    int occurenceCount = this.localOccurrenceCounts.get(key);
	    if (occurenceCount == -1)
		this.localOccurrenceCounts.put(key, 1);
	    else {
		this.localOccurrenceCounts.put(key, ++occurenceCount);
		((SourceType) handle).localOccurrenceCount = occurenceCount;
	    }
	}
    }

}

