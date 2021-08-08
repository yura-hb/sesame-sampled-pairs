class RequiresDirective extends ModuleDirective {
    /**
     * Returns the module name referenced by this declaration.
     *
     * @return the module referenced
     */
    public Name getName() {
	if (this.name == null) {
	    // lazy init must be thread-safe for readers
	    synchronized (this) {
		if (this.name == null) {
		    preLazyInit();
		    this.name = this.ast.newQualifiedName(new SimpleName(this.ast), new SimpleName(this.ast));
		    postLazyInit(this.name, NAME_PROPERTY);
		}
	    }
	}
	return this.name;
    }

    /**
     * The referenced module name; lazily initialized; defaults to a unspecified,
     * legal Java identifier.
     */
    private Name name = null;
    /**
     * The module structural property of this node type (child type: {@link Name}).
     */
    public static final ChildPropertyDescriptor NAME_PROPERTY = new ChildPropertyDescriptor(RequiresDirective.class,
	    "name", Name.class, OPTIONAL, NO_CYCLE_RISK);

}

