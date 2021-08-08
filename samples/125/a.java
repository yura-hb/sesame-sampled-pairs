class ArrayCreation extends Expression {
    /**
     * Returns the array type in this array creation expression.
     *
     * @return the array type
     */
    public ArrayType getType() {
	if (this.arrayType == null) {
	    // lazy init must be thread-safe for readers
	    synchronized (this) {
		if (this.arrayType == null) {
		    preLazyInit();
		    this.arrayType = this.ast.newArrayType(this.ast.newPrimitiveType(PrimitiveType.INT));
		    postLazyInit(this.arrayType, TYPE_PROPERTY);
		}
	    }
	}
	return this.arrayType;
    }

    /**
     * The array type; lazily initialized; defaults to a unspecified,
     * legal array type.
     */
    private ArrayType arrayType = null;
    /**
     * The "type" structural property of this node type (child type: {@link ArrayType}).
     * @since 3.0
     */
    public static final ChildPropertyDescriptor TYPE_PROPERTY = new ChildPropertyDescriptor(ArrayCreation.class, "type",
	    ArrayType.class, MANDATORY, NO_CYCLE_RISK);

}

