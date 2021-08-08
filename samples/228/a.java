import org.eclipse.jdt.internal.core.nd.field.FieldByte;

class NdTypeAnnotation extends NdAnnotation implements IDestructable {
    /**
     * @return one of the constants from {@link AnnotationTargetTypeConstants}
     */
    public int getTargetType() {
	return TARGET_TYPE.get(getNd(), this.address);
    }

    public static final FieldByte TARGET_TYPE;

}

