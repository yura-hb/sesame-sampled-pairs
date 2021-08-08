import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

class AnnotationMirrorImpl implements AnnotationMirror, EclipseMirrorObject {
    /**
     * @param memberName the name of the member
     * @return the value of the given member
     */
    public Object getValue(final String memberName) {
	if (memberName == null)
	    return null;
	final IMemberValuePairBinding[] declaredPairs = _domAnnotation.getDeclaredMemberValuePairs();
	for (IMemberValuePairBinding pair : declaredPairs) {
	    if (memberName.equals(pair.getName())) {
		return pair.getValue();
	    }
	}

	// didn't find it in the ast, check the default values.
	final IMethodBinding binding = getMethodBinding(memberName);
	if (binding == null)
	    return null;
	return binding.getDefaultValue();
    }

    /**The ast node that correspond to the annotation.*/
    private final IAnnotationBinding _domAnnotation;

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

}

