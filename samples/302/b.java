import org.eclipse.jdt.internal.core.nd.field.FieldString;

class NdType extends NdBinding {
    /**
     * Returns the missing type names as a comma-separated list
     */
    public IString getMissingTypeNames() {
	return MISSING_TYPE_NAMES.get(getNd(), this.address);
    }

    public static final FieldString MISSING_TYPE_NAMES;

}

