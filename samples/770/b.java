class MethodDeclaration extends BodyDeclaration {
    /**
     * Returns the name of the method declared in this method declaration.
     * For a constructor declaration, this should be the same as the name
     * of the class.
     *
     * @return the method name node
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
     * The method name; lazily initialized; defaults to an unspecified,
     * legal Java identifier.
     */
    private SimpleName methodName = null;
    /**
     * The "name" structural property of this node type (child type: {@link SimpleName}).
     * @since 3.0
     */
    public static final ChildPropertyDescriptor NAME_PROPERTY = new ChildPropertyDescriptor(MethodDeclaration.class,
	    "name", SimpleName.class, MANDATORY, NO_CYCLE_RISK);

}

