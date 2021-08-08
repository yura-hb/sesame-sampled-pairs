class ModuleModifier extends ASTNode {
    /**
     * Sets the module modifier keyword of this module modifier node.
     *
     * @param modifierKeyord the module modifier keyword
     * @exception IllegalArgumentException if the argument is &lt;code&gt;null&lt;/code&gt;
     */
    public void setKeyword(ModuleModifierKeyword modifierKeyord) {
	if (modifierKeyord == null) {
	    throw new IllegalArgumentException();
	}
	preValueChange(KEYWORD_PROPERTY);
	this.modifierKeyword = modifierKeyord;
	postValueChange(KEYWORD_PROPERTY);
    }

    /**
     * The "keyword" structural property of this node type (type: {@link ModuleModifier.ModuleModifierKeyword}).
     */
    public static final SimplePropertyDescriptor KEYWORD_PROPERTY = new SimplePropertyDescriptor(ModuleModifier.class,
	    "keyword", ModuleModifier.ModuleModifierKeyword.class, MANDATORY);
    /**
     * The modifier keyword; defaults to an unspecified modifier.
     */
    private ModuleModifierKeyword modifierKeyword = ModuleModifierKeyword.STATIC_KEYWORD;

}

