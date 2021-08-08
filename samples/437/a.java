import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;

class NdType extends NdBinding {
    /**
     * Sets the source name for this type.
     */
    public void setSourceNameOverride(char[] sourceName) {
	char[] oldSourceName = getSourceName();
	if (!CharArrayUtils.equals(oldSourceName, sourceName)) {
	    INNER_CLASS_SOURCE_NAME.put(getNd(), this.address, sourceName);
	}
    }

    public static final FieldString INNER_CLASS_SOURCE_NAME;
    public static final FieldManyToOne&lt;NdTypeId&gt; TYPENAME;

    public char[] getSourceName() {
	IString sourceName = getSourceNameOverride();
	if (sourceName.length() != 0) {
	    return sourceName.getChars();
	}
	char[] simpleName = getTypeId().getSimpleNameCharArray();
	return JavaNames.simpleNameToSourceName(simpleName);
    }

    public IString getSourceNameOverride() {
	return INNER_CLASS_SOURCE_NAME.get(getNd(), this.address);
    }

    public NdTypeId getTypeId() {
	return TYPENAME.get(getNd(), this.address);
    }

}

