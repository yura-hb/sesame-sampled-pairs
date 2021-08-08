abstract class FieldInstruction extends FieldOrMethod {
    /** @return size of field (1 or 2)
     */
    protected int getFieldSize(final ConstantPoolGen cpg) {
	return Type.size(Type.getTypeSize(getSignature(cpg)));
    }

}

