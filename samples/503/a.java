class ArrayType extends Type {
    /**
     * Sets the element type of the array.
     *
     * @param type the new type
     * @exception IllegalArgumentException if:
     * &lt;ul&gt;
     * &lt;li&gt;the node belongs to a different AST&lt;/li&gt;
     * &lt;li&gt;the node already has a parent&lt;/li&gt;
     * &lt;li&gt;the node is an array type&lt;/li&gt;
     * &lt;/ul&gt;
     * @exception UnsupportedOperationException if this operation is used below JLS8
     * @since 3.10
     */
    public void setElementType(Type type) {
	unsupportedIn2_3_4();
	if (type == null || type instanceof ArrayType) {
	    throw new IllegalArgumentException();
	}
	internalSetType(type, ELEMENT_TYPE_PROPERTY);
    }

    /**
     * The "elementType" structural property of this node type (child type: {@link Type}) (added in JLS8 API).
     * Cannot be an array type.
     * @since 3.10
     */
    public static final ChildPropertyDescriptor ELEMENT_TYPE_PROPERTY = new ChildPropertyDescriptor(ArrayType.class,
	    "elementType", Type.class, MANDATORY, CYCLE_RISK);
    /**
     * The element type (before JLS8: component type); lazily initialized; defaults to a simple type with
     * an unspecified, but legal, name.
     */
    private Type type = null;

    private void internalSetType(Type componentType, ChildPropertyDescriptor property) {
	ASTNode oldChild = this.type;
	preReplaceChild(oldChild, componentType, property);
	this.type = componentType;
	postReplaceChild(oldChild, componentType, property);
    }

}

