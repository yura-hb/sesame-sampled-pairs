import org.eclipse.jdt.internal.core.nd.field.FieldSearchKey;

class NdTypeId extends NdTypeSignature {
    /**
     * Returns the field descriptor.
     */
    public IString getFieldDescriptor() {
	return FIELD_DESCRIPTOR.get(getNd(), this.address);
    }

    public static final FieldSearchKey&lt;JavaIndex&gt; FIELD_DESCRIPTOR;

}

