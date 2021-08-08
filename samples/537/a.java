import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.*;

class ClassFile implements AttributeNamesConstants, CompilerModifiers, TypeConstants, TypeIds {
    /**
     * INTERNAL USE-ONLY
     * This methods generates the bytes for the field binding passed like a parameter
     * @param fieldBinding org.eclipse.jdt.internal.compiler.lookup.FieldBinding
     */
    public void addFieldInfo(FieldBinding fieldBinding) {
	int attributeNumber = 0;
	// check that there is enough space to write all the bytes for the field info corresponding
	// to the @fieldBinding
	if (contentsOffset + 30 &gt;= contents.length) {
	    resizeContents(30);
	}
	// Now we can generate all entries into the byte array
	// First the accessFlags
	int accessFlags = fieldBinding.getAccessFlags();
	if (targetJDK &lt; ClassFileConstants.JDK1_5) {
	    // pre 1.5, synthetic was an attribute, not a modifier
	    accessFlags &= ~AccSynthetic;
	}
	contents[contentsOffset++] = (byte) (accessFlags &gt;&gt; 8);
	contents[contentsOffset++] = (byte) accessFlags;
	// Then the nameIndex
	int nameIndex = constantPool.literalIndex(fieldBinding.name);
	contents[contentsOffset++] = (byte) (nameIndex &gt;&gt; 8);
	contents[contentsOffset++] = (byte) nameIndex;
	// Then the descriptorIndex
	int descriptorIndex = constantPool.literalIndex(fieldBinding.type.signature());
	contents[contentsOffset++] = (byte) (descriptorIndex &gt;&gt; 8);
	contents[contentsOffset++] = (byte) descriptorIndex;
	// leave some space for the number of attributes
	int fieldAttributeOffset = contentsOffset;
	contentsOffset += 2;
	// 4.7.2 only static constant fields get a ConstantAttribute
	// Generate the constantValueAttribute
	if (fieldBinding.isConstantValue()) {
	    if (contentsOffset + 8 &gt;= contents.length) {
		resizeContents(8);
	    }
	    // Now we generate the constant attribute corresponding to the fieldBinding
	    int constantValueNameIndex = constantPool.literalIndex(AttributeNamesConstants.ConstantValueName);
	    contents[contentsOffset++] = (byte) (constantValueNameIndex &gt;&gt; 8);
	    contents[contentsOffset++] = (byte) constantValueNameIndex;
	    // The attribute length = 2 in case of a constantValue attribute
	    contents[contentsOffset++] = 0;
	    contents[contentsOffset++] = 0;
	    contents[contentsOffset++] = 0;
	    contents[contentsOffset++] = 2;
	    attributeNumber++;
	    // Need to add the constant_value_index
	    Constant fieldConstant = fieldBinding.constant();
	    switch (fieldConstant.typeID()) {
	    case T_boolean:
		int booleanValueIndex = constantPool.literalIndex(fieldConstant.booleanValue() ? 1 : 0);
		contents[contentsOffset++] = (byte) (booleanValueIndex &gt;&gt; 8);
		contents[contentsOffset++] = (byte) booleanValueIndex;
		break;
	    case T_byte:
	    case T_char:
	    case T_int:
	    case T_short:
		int integerValueIndex = constantPool.literalIndex(fieldConstant.intValue());
		contents[contentsOffset++] = (byte) (integerValueIndex &gt;&gt; 8);
		contents[contentsOffset++] = (byte) integerValueIndex;
		break;
	    case T_float:
		int floatValueIndex = constantPool.literalIndex(fieldConstant.floatValue());
		contents[contentsOffset++] = (byte) (floatValueIndex &gt;&gt; 8);
		contents[contentsOffset++] = (byte) floatValueIndex;
		break;
	    case T_double:
		int doubleValueIndex = constantPool.literalIndex(fieldConstant.doubleValue());
		contents[contentsOffset++] = (byte) (doubleValueIndex &gt;&gt; 8);
		contents[contentsOffset++] = (byte) doubleValueIndex;
		break;
	    case T_long:
		int longValueIndex = constantPool.literalIndex(fieldConstant.longValue());
		contents[contentsOffset++] = (byte) (longValueIndex &gt;&gt; 8);
		contents[contentsOffset++] = (byte) longValueIndex;
		break;
	    case T_String:
		int stringValueIndex = constantPool.literalIndex(((StringConstant) fieldConstant).stringValue());
		if (stringValueIndex == -1) {
		    if (!creatingProblemType) {
			// report an error and abort: will lead to a problem type classfile creation
			TypeDeclaration typeDeclaration = referenceBinding.scope.referenceContext;
			FieldDeclaration[] fieldDecls = typeDeclaration.fields;
			for (int i = 0, max = fieldDecls.length; i &lt; max; i++) {
			    if (fieldDecls[i].binding == fieldBinding) {
				// problem should abort
				typeDeclaration.scope.problemReporter()
					.stringConstantIsExceedingUtf8Limit(fieldDecls[i]);
			    }
			}
		    } else {
			// already inside a problem type creation : no constant for this field
			contentsOffset = fieldAttributeOffset + 2;
			// +2 is necessary to keep the two byte space for the attribute number
			attributeNumber--;
		    }
		} else {
		    contents[contentsOffset++] = (byte) (stringValueIndex &gt;&gt; 8);
		    contents[contentsOffset++] = (byte) stringValueIndex;
		}
	    }
	}
	if (this.targetJDK &lt; ClassFileConstants.JDK1_5 && fieldBinding.isSynthetic()) {
	    if (contentsOffset + 6 &gt;= contents.length) {
		resizeContents(6);
	    }
	    int syntheticAttributeNameIndex = constantPool.literalIndex(AttributeNamesConstants.SyntheticName);
	    contents[contentsOffset++] = (byte) (syntheticAttributeNameIndex &gt;&gt; 8);
	    contents[contentsOffset++] = (byte) syntheticAttributeNameIndex;
	    // the length of a synthetic attribute is equals to 0
	    contents[contentsOffset++] = 0;
	    contents[contentsOffset++] = 0;
	    contents[contentsOffset++] = 0;
	    contents[contentsOffset++] = 0;
	    attributeNumber++;
	}
	if (fieldBinding.isDeprecated()) {
	    if (contentsOffset + 6 &gt;= contents.length) {
		resizeContents(6);
	    }
	    int deprecatedAttributeNameIndex = constantPool.literalIndex(AttributeNamesConstants.DeprecatedName);
	    contents[contentsOffset++] = (byte) (deprecatedAttributeNameIndex &gt;&gt; 8);
	    contents[contentsOffset++] = (byte) deprecatedAttributeNameIndex;
	    // the length of a deprecated attribute is equals to 0
	    contents[contentsOffset++] = 0;
	    contents[contentsOffset++] = 0;
	    contents[contentsOffset++] = 0;
	    contents[contentsOffset++] = 0;
	    attributeNumber++;
	}
	// add signature attribute
	char[] genericSignature = fieldBinding.genericSignature();
	if (genericSignature != null) {
	    // check that there is enough space to write all the bytes for the field info corresponding
	    // to the @fieldBinding
	    if (contentsOffset + 8 &gt;= contents.length) {
		resizeContents(8);
	    }
	    int signatureAttributeNameIndex = constantPool.literalIndex(AttributeNamesConstants.SignatureName);
	    contents[contentsOffset++] = (byte) (signatureAttributeNameIndex &gt;&gt; 8);
	    contents[contentsOffset++] = (byte) signatureAttributeNameIndex;
	    // the length of a signature attribute is equals to 2
	    contents[contentsOffset++] = 0;
	    contents[contentsOffset++] = 0;
	    contents[contentsOffset++] = 0;
	    contents[contentsOffset++] = 2;
	    int signatureIndex = constantPool.literalIndex(genericSignature);
	    contents[contentsOffset++] = (byte) (signatureIndex &gt;&gt; 8);
	    contents[contentsOffset++] = (byte) signatureIndex;
	    attributeNumber++;
	}
	contents[fieldAttributeOffset++] = (byte) (attributeNumber &gt;&gt; 8);
	contents[fieldAttributeOffset] = (byte) attributeNumber;
    }

    public int contentsOffset;
    public byte[] contents;
    public long targetJDK;
    public ConstantPool constantPool;
    protected boolean creatingProblemType;
    public SourceTypeBinding referenceBinding;

    /**
     * Resize the pool contents
     */
    private final void resizeContents(int minimalSize) {
	int length = this.contents.length;
	int toAdd = length;
	if (toAdd &lt; minimalSize)
	    toAdd = minimalSize;
	System.arraycopy(this.contents, 0, this.contents = new byte[length + toAdd], 0, length);
    }

}

