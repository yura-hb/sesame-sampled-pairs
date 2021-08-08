class ClassDeclaration implements Constants {
    /**
     * Set the class definition
     */
    public void setDefinition(ClassDefinition definition, int status) {

	// Sanity checks.

	// The name of the definition should match that of the declaration.
	if ((definition != null) && !getName().equals(definition.getName())) {
	    throw new CompilerError("setDefinition: name mismatch: " + this + ", " + definition);
	}

	// The status states can be considered ordered in the same
	// manner as their numerical values. We expect classes to
	// progress through a sequence of monotonically increasing
	// states. NOTE: There are currently exceptions to this rule
	// which are believed to be legitimate.  In particular, a
	// class may be checked more than once, though we believe that
	// this is unnecessary and may be avoided.
	/*-----------------*
	if (status &lt;= this.status) {
	    System.out.println("STATUS REGRESSION: " +
	                       this + " FROM " + this.status + " TO " + status);
	}
	*------------------*/

	this.definition = definition;
	this.status = status;
    }

    ClassDefinition definition;
    int status;
    Type type;

    /**
     * Get the name of the class
     */
    public Identifier getName() {
	return type.getClassName();
    }

}

