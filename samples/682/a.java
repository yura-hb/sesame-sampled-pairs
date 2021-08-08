import org.eclipse.jdt.internal.core.nd.field.FieldString;

class NdComplexTypeSignature extends NdTypeSignature {
    /**
     * If this type is a type variable, this returns the variable's identifier.
     */
    public IString getVariableIdentifier() {
	return VARIABLE_IDENTIFIER.get(getNd(), this.address);
    }

    public static final FieldString VARIABLE_IDENTIFIER;

}

