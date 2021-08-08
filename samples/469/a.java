class ConditionalExpression extends Expression {
    /**
     * Returns the condition of this conditional expression.
     *
     * @return the condition node
     */
    public Expression getExpression() {
	if (this.conditionExpression == null) {
	    // lazy init must be thread-safe for readers
	    synchronized (this) {
		if (this.conditionExpression == null) {
		    preLazyInit();
		    this.conditionExpression = new SimpleName(this.ast);
		    postLazyInit(this.conditionExpression, EXPRESSION_PROPERTY);
		}
	    }
	}
	return this.conditionExpression;
    }

    /**
     * The condition expression; lazily initialized; defaults to an unspecified,
     * but legal, expression.
     */
    private Expression conditionExpression = null;
    /**
     * The "expression" structural property of this node type (child type: {@link Expression}).
     * @since 3.0
     */
    public static final ChildPropertyDescriptor EXPRESSION_PROPERTY = new ChildPropertyDescriptor(
	    ConditionalExpression.class, "expression", Expression.class, MANDATORY, CYCLE_RISK);

}

