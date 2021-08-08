import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.jdt.apt.core.internal.declaration.EclipseMirrorObject.MirrorKind;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.TypeParameter;

class ExecutableUtil {
    /**
     * @param executable must be a constructor, method or annotation element.
     * @return the formal type parameters of the executable. 
     */
    static Collection&lt;TypeParameterDeclaration&gt; getFormalTypeParameters(EclipseDeclarationImpl executable,
	    BaseProcessorEnv env) {
	// the dom ast does not provide type parameter list for annotation element
	// that incorrectly includes them in the text
	if (executable == null || executable.kind() == MirrorKind.ANNOTATION_ELEMENT)
	    return Collections.emptyList();
	if (executable.kind() != MirrorKind.METHOD && executable.kind() != MirrorKind.CONSTRUCTOR)
	    throw new IllegalArgumentException("Executable is not a method " + //$NON-NLS-1$
		    executable.getClass().getName());

	if (executable.isFromSource()) {
	    final org.eclipse.jdt.core.dom.MethodDeclaration methodAstNode = (org.eclipse.jdt.core.dom.MethodDeclaration) executable
		    .getAstNode();

	    // Synthetic methods will have no ast node
	    if (methodAstNode == null)
		return Collections.emptyList();
	    final List&lt;TypeParameter&gt; typeParams = methodAstNode.typeParameters();
	    final List&lt;TypeParameterDeclaration&gt; result = new ArrayList&lt;&gt;();
	    for (TypeParameter typeParam : typeParams) {
		final ITypeBinding typeBinding = typeParam.resolveBinding();
		if (typeBinding == null) {
		    throw new UnsupportedOperationException(
			    "cannot create a type parameter declaration without a binding"); //$NON-NLS-1$
		} else {
		    final TypeParameterDeclaration typeParamDecl = (TypeParameterDeclaration) Factory
			    .createDeclaration(typeBinding, env);
		    if (typeParamDecl != null)
			result.add(typeParamDecl);
		}
	    }
	    return result;
	} else { // binary
	    if (!executable.isBindingBased())
		throw new IllegalStateException("binary executable without binding."); //$NON-NLS-1$
	    final IMethodBinding methodBinding = ((ExecutableDeclarationImpl) executable).getDeclarationBinding();
	    final ITypeBinding[] typeParams = methodBinding.getTypeParameters();
	    if (typeParams == null || typeParams.length == 0)
		return Collections.emptyList();
	    final List&lt;TypeParameterDeclaration&gt; result = new ArrayList&lt;&gt;();
	    for (ITypeBinding typeVar : typeParams) {
		final TypeParameterDeclaration typeParamDecl = (TypeParameterDeclaration) Factory
			.createDeclaration(typeVar, env);
		if (typeParamDecl != null)
		    result.add(typeParamDecl);
	    }
	    return result;

	}
    }

}

