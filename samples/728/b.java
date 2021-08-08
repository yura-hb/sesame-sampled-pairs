abstract class MethodVisitor {
    /**
     * Visits a non standard attribute of this method.
     *
     * @param attr
     *            an attribute.
     */
    public void visitAttribute(Attribute attr) {
	if (mv != null) {
	    mv.visitAttribute(attr);
	}
    }

    /**
     * The method visitor to which this visitor must delegate method calls. May
     * be null.
     */
    protected MethodVisitor mv;

}

