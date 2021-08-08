class InstanceofExpression extends Expression {
    /**
     * Sets the right operand of this instanceof expression.
     *
     * @param referenceType the right operand node
     * @exception IllegalArgumentException if:
     * &lt;ul&gt;
     * &lt;li&gt;the node belongs to a different AST&lt;/li&gt;
     * &lt;li&gt;the node already has a parent&lt;/li&gt;
     * &lt;li&gt;a cycle in would be created&lt;/li&gt;
     * &lt;/ul&gt;
     */
    public void setRightOperand(Type referenceType) {
	if (referenceType == null) {
	    throw new IllegalArgumentException();
	}
	ASTNode oldChild = this.rightOperand;
	preReplaceChild(oldChild, referenceType, RIGHT_OPERAND_PROPERTY);
	this.rightOperand = referenceType;
	postReplaceChild(oldChild, referenceType, RIGHT_OPERAND_PROPERTY);
    }

    /**
     * The right operand; lazily initialized; defaults to an unspecified,
     * but legal, simple type.
     */
    private Type rightOperand = null;
    /**
     * The "rightOperand" structural property of this node type (child type: {@link Type}).
     * @since 3.0
     */
    public static final ChildPropertyDescriptor RIGHT_OPERAND_PROPERTY = new ChildPropertyDescriptor(
	    InstanceofExpression.class, "rightOperand", Type.class, MANDATORY, CYCLE_RISK);

}

