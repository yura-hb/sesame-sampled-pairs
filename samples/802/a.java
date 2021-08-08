abstract class ModulePackageAccess extends ModuleDirective {
    /**
     * Returns the name of the package.
     *
     * @return the package name node
     */
    public Name getName() {
	if (this.name == null) {
	    // lazy init must be thread-safe for readers
	    synchronized (this) {
		if (this.name == null) {
		    preLazyInit();
		    this.name = this.ast.newQualifiedName(new SimpleName(this.ast), new SimpleName(this.ast));
		    ChildPropertyDescriptor p = internalNameProperty();
		    postLazyInit(this.name, p);
		}
	    }
	}
	return this.name;
    }

    /**
     * The package name; lazily initialized; defaults to a unspecified,
     * legal Java identifier.
     */
    protected Name name = null;

    /**
     * Returns structural property descriptor for the "name" property
     * of this node (child type: {@link Name}).
     *
     * @return the property descriptor
     */
    abstract ChildPropertyDescriptor internalNameProperty();

}

