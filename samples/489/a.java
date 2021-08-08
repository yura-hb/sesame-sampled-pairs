class LabeledStatement extends Statement {
    /**
     * Sets the label of this labeled statement.
     *
     * @param label the new label
     * @exception IllegalArgumentException if:
     * &lt;ul&gt;
     * &lt;li&gt;the node belongs to a different AST&lt;/li&gt;
     * &lt;li&gt;the node already has a parent&lt;/li&gt;
     * &lt;/ul&gt;
     */
    public void setLabel(SimpleName label) {
	if (label == null) {
	    throw new IllegalArgumentException();
	}
	ASTNode oldChild = this.labelName;
	preReplaceChild(oldChild, label, LABEL_PROPERTY);
	this.labelName = label;
	postReplaceChild(oldChild, label, LABEL_PROPERTY);
    }

    /**
     * The label; lazily initialized; defaults to a unspecified,
     * legal Java identifier.
     */
    private SimpleName labelName = null;
    /**
     * The "label" structural property of this node type (child type: {@link SimpleName}).
     * @since 3.0
     */
    public static final ChildPropertyDescriptor LABEL_PROPERTY = new ChildPropertyDescriptor(LabeledStatement.class,
	    "label", SimpleName.class, MANDATORY, NO_CYCLE_RISK);

}

