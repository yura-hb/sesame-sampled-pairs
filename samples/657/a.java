import org.eclipse.jdt.core.*;

class Util {
    /**
     * Returns the IInitializer that contains the given local variable in the given type
     */
    public static JavaElement getUnresolvedJavaElement(int localSourceStart, int localSourceEnd, JavaElement type) {
	try {
	    if (!(type instanceof IType))
		return null;
	    IInitializer[] initializers = ((IType) type).getInitializers();
	    for (int i = 0; i &lt; initializers.length; i++) {
		IInitializer initializer = initializers[i];
		ISourceRange sourceRange = initializer.getSourceRange();
		if (sourceRange != null) {
		    int initializerStart = sourceRange.getOffset();
		    int initializerEnd = initializerStart + sourceRange.getLength();
		    if (initializerStart &lt;= localSourceStart && localSourceEnd &lt;= initializerEnd) {
			return (JavaElement) initializer;
		    }
		}
	    }
	    return null;
	} catch (JavaModelException e) {
	    return null;
	}
    }

}

