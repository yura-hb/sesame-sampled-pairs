class MethodRef extends ASTNode implements IDocElement {
    /**
     * Returns the name of the referenced method or constructor.
     *
     * @return the method or constructor name node
     */
    public SimpleName getName() {
	if (this.methodName == null) {
	    // lazy init must be thread-safe for readers
	    synchronized (this) {
		if (this.methodName == null) {
		    preLazyInit();
		    this.methodName = new SimpleName(this.ast);
		    postLazyInit(this.methodName, NAME_PROPERTY);
		}
	    }
	}
	return this.methodName;
    }

    /**
     * The method name; lazily initialized; defaults to a unspecified,
     * legal Java method name.
     */
    private SimpleName methodName = null;
    /**
     * The "name" structural property of this node type (child type: {@link SimpleName}).
     * @since 3.0
     */
    public static final ChildPropertyDescriptor NAME_PROPERTY = new ChildPropertyDescriptor(MethodRef.class, "name",
	    SimpleName.class, MANDATORY, NO_CYCLE_RISK);

}

