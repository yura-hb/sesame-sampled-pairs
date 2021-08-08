import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

class AnnotationMirrorImpl implements AnnotationMirror, EclipseMirrorObject {
    /**
     * @return the method binding that matches the given name from the annotation type
     *         referenced by this annotation.
     */
    public IMethodBinding getMethodBinding(final String memberName) {
	if (memberName == null)
	    return null;
	final ITypeBinding typeBinding = _domAnnotation.getAnnotationType();
	if (typeBinding == null)
	    return null;
	final IMethodBinding[] methods = typeBinding.getDeclaredMethods();
	for (IMethodBinding method : methods) {
	    if (memberName.equals(method.getName()))
		return method;
	}
	return null;
    }

    /**The ast node that correspond to the annotation.*/
    private final IAnnotationBinding _domAnnotation;

}

