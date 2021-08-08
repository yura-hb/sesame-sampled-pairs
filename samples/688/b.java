class MemberDefinition implements Constants {
    /**
     * Tells whether to report a deprecation error for this field.
     */
    public boolean reportDeprecated(Environment env) {
	return (isDeprecated() || clazz.reportDeprecated(env));
    }

    protected ClassDefinition clazz;
    protected int modifiers;

    public final boolean isDeprecated() {
	return (modifiers & M_DEPRECATED) != 0;
    }

}

