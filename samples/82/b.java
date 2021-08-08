import sun.reflect.generics.tree.FormalTypeParameter;
import sun.reflect.generics.tree.Signature;
import sun.reflect.generics.visitor.Reifier;

abstract class GenericDeclRepository&lt;S&gt; extends AbstractRepository&lt;S&gt; {
    /**
     * Returns the formal type parameters of this generic declaration.
     * @return the formal type parameters of this generic declaration
     */
    public TypeVariable&lt;?&gt;[] getTypeParameters() {
	TypeVariable&lt;?&gt;[] value = typeParameters;
	if (value == null) {
	    value = computeTypeParameters();
	    typeParameters = value;
	}
	return value.clone();
    }

    /** The formal type parameters.  Lazily initialized. */
    private volatile TypeVariable&lt;?&gt;[] typeParameters;

    private TypeVariable&lt;?&gt;[] computeTypeParameters() {
	// first, extract type parameter subtree(s) from AST
	FormalTypeParameter[] ftps = getTree().getFormalTypeParameters();
	// create array to store reified subtree(s)
	int length = ftps.length;
	TypeVariable&lt;?&gt;[] typeParameters = new TypeVariable&lt;?&gt;[length];
	// reify all subtrees
	for (int i = 0; i &lt; length; i++) {
	    Reifier r = getReifier(); // obtain visitor
	    ftps[i].accept(r); // reify subtree
	    // extract result from visitor and store it
	    typeParameters[i] = (TypeVariable&lt;?&gt;) r.getResult();
	}
	return typeParameters;
    }

}

