class IdentifierExpression extends Expression {
    /**
     * Return an updater if one is needed for assignments to this expression.
     */
    public FieldUpdater getUpdater(Environment env, Context ctx) {
	if (implementation != null)
	    return implementation.getUpdater(env, ctx);
	return null;
    }

    Expression implementation;

}

