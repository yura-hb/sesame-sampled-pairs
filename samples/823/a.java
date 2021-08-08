import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MemberValuePair;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.AnnotationContext;
import org.eclipse.jdt.internal.compiler.codegen.AnnotationTargetTypeConstants;
import org.eclipse.jdt.internal.compiler.codegen.AttributeNamesConstants;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

class ClassFile implements TypeConstants, TypeIds {
    /**
     * INTERNAL USE-ONLY
     * This methods generate all the fields infos for the receiver.
     * This includes:
     * - a field info for each defined field of that class
     * - a field info for each synthetic field (e.g. this$0)
     */
    public void addFieldInfos() {
	SourceTypeBinding currentBinding = this.referenceBinding;
	FieldBinding[] syntheticFields = currentBinding.syntheticFields();
	int fieldCount = currentBinding.fieldCount() + (syntheticFields == null ? 0 : syntheticFields.length);

	// write the number of fields
	if (fieldCount &gt; 0xFFFF) {
	    this.referenceBinding.scope.problemReporter().tooManyFields(this.referenceBinding.scope.referenceType());
	}
	this.contents[this.contentsOffset++] = (byte) (fieldCount &gt;&gt; 8);
	this.contents[this.contentsOffset++] = (byte) fieldCount;

	FieldDeclaration[] fieldDecls = currentBinding.scope.referenceContext.fields;
	for (int i = 0, max = fieldDecls == null ? 0 : fieldDecls.length; i &lt; max; i++) {
	    FieldDeclaration fieldDecl = fieldDecls[i];
	    if (fieldDecl.binding != null) {
		addFieldInfo(fieldDecl.binding);
	    }
	}

	if (syntheticFields != null) {
	    for (int i = 0, max = syntheticFields.length; i &lt; max; i++) {
		addFieldInfo(syntheticFields[i]);
	    }
	}
    }

    public SourceTypeBinding referenceBinding;
    public byte[] contents;
    public int contentsOffset;
    public long targetJDK;
    public ConstantPool constantPool;
    public int produceAttributes;
    public List&lt;TypeBinding&gt; missingTypes = null;
    protected boolean creatingProblemType;
    public Map&lt;TypeBinding, Boolean&gt; innerClassesBindings;
    public static final int INNER_CLASSES_SIZE = 5;

    /**
     * INTERNAL USE-ONLY
     * This methods generates the bytes for the given field binding
     * @param fieldBinding the given field binding
     */
    private void addFieldInfo(FieldBinding fieldBinding) {
	// check that there is enough space to write all the bytes for the field info corresponding
	// to the @fieldBinding
	if (this.contentsOffset + 8 &gt;= this.contents.length) {
	    resizeContents(8);
	}
	// Now we can generate all entries into the byte array
	// First the accessFlags
	int accessFlags = fieldBinding.getAccessFlags();
	if (this.targetJDK &lt; ClassFileConstants.JDK1_5) {
	    // pre 1.5, synthetic was an attribute, not a modifier
	    accessFlags &= ~ClassFileConstants.AccSynthetic;
	}
	this.contents[this.contentsOffset++] = (byte) (accessFlags &gt;&gt; 8);
	this.contents[this.contentsOffset++] = (byte) accessFlags;
	// Then the nameIndex
	int nameIndex = this.constantPool.literalIndex(fieldBinding.name);
	this.contents[this.contentsOffset++] = (byte) (nameIndex &gt;&gt; 8);
	this.contents[this.contentsOffset++] = (byte) nameIndex;
	// Then the descriptorIndex
	int descriptorIndex = this.constantPool.literalIndex(fieldBinding.type);
	this.contents[this.contentsOffset++] = (byte) (descriptorIndex &gt;&gt; 8);
	this.contents[this.contentsOffset++] = (byte) descriptorIndex;
	int fieldAttributeOffset = this.contentsOffset;
	int attributeNumber = 0;
	// leave some space for the number of attributes
	this.contentsOffset += 2;
	attributeNumber += addFieldAttributes(fieldBinding, fieldAttributeOffset);
	if (this.contentsOffset + 2 &gt;= this.contents.length) {
	    resizeContents(2);
	}
	this.contents[fieldAttributeOffset++] = (byte) (attributeNumber &gt;&gt; 8);
	this.contents[fieldAttributeOffset] = (byte) attributeNumber;
    }

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

    private int addFieldAttributes(FieldBinding fieldBinding, int fieldAttributeOffset) {
	int attributesNumber = 0;
	// 4.7.2 only static constant fields get a ConstantAttribute
	// Generate the constantValueAttribute
	Constant fieldConstant = fieldBinding.constant();
	if (fieldConstant != Constant.NotAConstant) {
	    attributesNumber += generateConstantValueAttribute(fieldConstant, fieldBinding, fieldAttributeOffset);
	}
	if (this.targetJDK &lt; ClassFileConstants.JDK1_5 && fieldBinding.isSynthetic()) {
	    attributesNumber += generateSyntheticAttribute();
	}
	if (fieldBinding.isDeprecated()) {
	    attributesNumber += generateDeprecatedAttribute();
	}
	// add signature attribute
	char[] genericSignature = fieldBinding.genericSignature();
	if (genericSignature != null) {
	    attributesNumber += generateSignatureAttribute(genericSignature);
	}
	if (this.targetJDK &gt;= ClassFileConstants.JDK1_4) {
	    FieldDeclaration fieldDeclaration = fieldBinding.sourceField();
	    if (fieldDeclaration != null) {
		Annotation[] annotations = fieldDeclaration.annotations;
		if (annotations != null) {
		    attributesNumber += generateRuntimeAnnotations(annotations, TagBits.AnnotationForField);
		}

		if ((this.produceAttributes & ClassFileConstants.ATTR_TYPE_ANNOTATION) != 0) {
		    List allTypeAnnotationContexts = new ArrayList();
		    if (annotations != null && (fieldDeclaration.bits & ASTNode.HasTypeAnnotations) != 0) {
			fieldDeclaration.getAllAnnotationContexts(AnnotationTargetTypeConstants.FIELD,
				allTypeAnnotationContexts);
		    }
		    int invisibleTypeAnnotationsCounter = 0;
		    int visibleTypeAnnotationsCounter = 0;
		    TypeReference fieldType = fieldDeclaration.type;
		    if (fieldType != null && ((fieldType.bits & ASTNode.HasTypeAnnotations) != 0)) {
			fieldType.getAllAnnotationContexts(AnnotationTargetTypeConstants.FIELD,
				allTypeAnnotationContexts);
		    }
		    int size = allTypeAnnotationContexts.size();
		    if (size != 0) {
			AnnotationContext[] allTypeAnnotationContextsArray = new AnnotationContext[size];
			allTypeAnnotationContexts.toArray(allTypeAnnotationContextsArray);
			for (int i = 0, max = allTypeAnnotationContextsArray.length; i &lt; max; i++) {
			    AnnotationContext annotationContext = allTypeAnnotationContextsArray[i];
			    if ((annotationContext.visibility & AnnotationContext.INVISIBLE) != 0) {
				invisibleTypeAnnotationsCounter++;
				allTypeAnnotationContexts.add(annotationContext);
			    } else {
				visibleTypeAnnotationsCounter++;
				allTypeAnnotationContexts.add(annotationContext);
			    }
			}
			attributesNumber += generateRuntimeTypeAnnotations(allTypeAnnotationContextsArray,
				visibleTypeAnnotationsCounter, invisibleTypeAnnotationsCounter);
		    }
		}
	    }
	}
	if ((fieldBinding.tagBits & TagBits.HasMissingType) != 0) {
	    this.missingTypes = fieldBinding.type.collectMissingTypes(this.missingTypes);
	}
	return attributesNumber;
    }

    private int generateConstantValueAttribute(Constant fieldConstant, FieldBinding fieldBinding,
	    int fieldAttributeOffset) {
	int localContentsOffset = this.contentsOffset;
	int attributesNumber = 1;
	if (localContentsOffset + 8 &gt;= this.contents.length) {
	    resizeContents(8);
	}
	// Now we generate the constant attribute corresponding to the fieldBinding
	int constantValueNameIndex = this.constantPool.literalIndex(AttributeNamesConstants.ConstantValueName);
	this.contents[localContentsOffset++] = (byte) (constantValueNameIndex &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) constantValueNameIndex;
	// The attribute length = 2 in case of a constantValue attribute
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 2;
	// Need to add the constant_value_index
	switch (fieldConstant.typeID()) {
	case T_boolean:
	    int booleanValueIndex = this.constantPool.literalIndex(fieldConstant.booleanValue() ? 1 : 0);
	    this.contents[localContentsOffset++] = (byte) (booleanValueIndex &gt;&gt; 8);
	    this.contents[localContentsOffset++] = (byte) booleanValueIndex;
	    break;
	case T_byte:
	case T_char:
	case T_int:
	case T_short:
	    int integerValueIndex = this.constantPool.literalIndex(fieldConstant.intValue());
	    this.contents[localContentsOffset++] = (byte) (integerValueIndex &gt;&gt; 8);
	    this.contents[localContentsOffset++] = (byte) integerValueIndex;
	    break;
	case T_float:
	    int floatValueIndex = this.constantPool.literalIndex(fieldConstant.floatValue());
	    this.contents[localContentsOffset++] = (byte) (floatValueIndex &gt;&gt; 8);
	    this.contents[localContentsOffset++] = (byte) floatValueIndex;
	    break;
	case T_double:
	    int doubleValueIndex = this.constantPool.literalIndex(fieldConstant.doubleValue());
	    this.contents[localContentsOffset++] = (byte) (doubleValueIndex &gt;&gt; 8);
	    this.contents[localContentsOffset++] = (byte) doubleValueIndex;
	    break;
	case T_long:
	    int longValueIndex = this.constantPool.literalIndex(fieldConstant.longValue());
	    this.contents[localContentsOffset++] = (byte) (longValueIndex &gt;&gt; 8);
	    this.contents[localContentsOffset++] = (byte) longValueIndex;
	    break;
	case T_JavaLangString:
	    int stringValueIndex = this.constantPool.literalIndex(((StringConstant) fieldConstant).stringValue());
	    if (stringValueIndex == -1) {
		if (!this.creatingProblemType) {
		    // report an error and abort: will lead to a problem type classfile creation
		    TypeDeclaration typeDeclaration = this.referenceBinding.scope.referenceContext;
		    FieldDeclaration[] fieldDecls = typeDeclaration.fields;
		    int max = fieldDecls == null ? 0 : fieldDecls.length;
		    for (int i = 0; i &lt; max; i++) {
			if (fieldDecls[i].binding == fieldBinding) {
			    // problem should abort
			    typeDeclaration.scope.problemReporter().stringConstantIsExceedingUtf8Limit(fieldDecls[i]);
			}
		    }
		} else {
		    // already inside a problem type creation : no constant for this field
		    this.contentsOffset = fieldAttributeOffset;
		    attributesNumber = 0;
		}
	    } else {
		this.contents[localContentsOffset++] = (byte) (stringValueIndex &gt;&gt; 8);
		this.contents[localContentsOffset++] = (byte) stringValueIndex;
	    }
	}
	this.contentsOffset = localContentsOffset;
	return attributesNumber;
    }

    private int generateSyntheticAttribute() {
	int localContentsOffset = this.contentsOffset;
	if (localContentsOffset + 6 &gt;= this.contents.length) {
	    resizeContents(6);
	}
	int syntheticAttributeNameIndex = this.constantPool.literalIndex(AttributeNamesConstants.SyntheticName);
	this.contents[localContentsOffset++] = (byte) (syntheticAttributeNameIndex &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) syntheticAttributeNameIndex;
	// the length of a synthetic attribute is equals to 0
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contentsOffset = localContentsOffset;
	return 1;
    }

    private int generateDeprecatedAttribute() {
	int localContentsOffset = this.contentsOffset;
	if (localContentsOffset + 6 &gt;= this.contents.length) {
	    resizeContents(6);
	}
	int deprecatedAttributeNameIndex = this.constantPool.literalIndex(AttributeNamesConstants.DeprecatedName);
	this.contents[localContentsOffset++] = (byte) (deprecatedAttributeNameIndex &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) deprecatedAttributeNameIndex;
	// the length of a deprecated attribute is equals to 0
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contentsOffset = localContentsOffset;
	return 1;
    }

    private int generateSignatureAttribute(char[] genericSignature) {
	int localContentsOffset = this.contentsOffset;
	if (localContentsOffset + 8 &gt;= this.contents.length) {
	    resizeContents(8);
	}
	int signatureAttributeNameIndex = this.constantPool.literalIndex(AttributeNamesConstants.SignatureName);
	this.contents[localContentsOffset++] = (byte) (signatureAttributeNameIndex &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) signatureAttributeNameIndex;
	// the length of a signature attribute is equals to 2
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 2;
	int signatureIndex = this.constantPool.literalIndex(genericSignature);
	this.contents[localContentsOffset++] = (byte) (signatureIndex &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) signatureIndex;
	this.contentsOffset = localContentsOffset;
	return 1;
    }

    /**
     * @param annotations
     * @param targetMask allowed targets
     * @return the number of attributes created while dumping the annotations in the .class file
     */
    private int generateRuntimeAnnotations(final Annotation[] annotations, final long targetMask) {
	int attributesNumber = 0;
	final int length = annotations.length;
	int visibleAnnotationsCounter = 0;
	int invisibleAnnotationsCounter = 0;
	for (int i = 0; i &lt; length; i++) {
	    Annotation annotation;
	    if ((annotation = annotations[i].getPersistibleAnnotation()) == null)
		continue; // already packaged into container.
	    long annotationMask = annotation.resolvedType != null
		    ? annotation.resolvedType.getAnnotationTagBits() & TagBits.AnnotationTargetMASK
		    : 0;
	    if (annotationMask != 0 && (annotationMask & targetMask) == 0) {
		if (!jdk16packageInfoAnnotation(annotationMask, targetMask))
		    continue;
	    }
	    if (annotation.isRuntimeInvisible() || annotation.isRuntimeTypeInvisible()) {
		invisibleAnnotationsCounter++;
	    } else if (annotation.isRuntimeVisible() || annotation.isRuntimeTypeVisible()) {
		visibleAnnotationsCounter++;
	    }
	}

	int annotationAttributeOffset = this.contentsOffset;
	if (invisibleAnnotationsCounter != 0) {
	    if (this.contentsOffset + 10 &gt;= this.contents.length) {
		resizeContents(10);
	    }
	    int runtimeInvisibleAnnotationsAttributeNameIndex = this.constantPool
		    .literalIndex(AttributeNamesConstants.RuntimeInvisibleAnnotationsName);
	    this.contents[this.contentsOffset++] = (byte) (runtimeInvisibleAnnotationsAttributeNameIndex &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) runtimeInvisibleAnnotationsAttributeNameIndex;
	    int attributeLengthOffset = this.contentsOffset;
	    this.contentsOffset += 4; // leave space for the attribute length

	    int annotationsLengthOffset = this.contentsOffset;
	    this.contentsOffset += 2; // leave space for the annotations length

	    int counter = 0;
	    loop: for (int i = 0; i &lt; length; i++) {
		if (invisibleAnnotationsCounter == 0)
		    break loop;
		Annotation annotation;
		if ((annotation = annotations[i].getPersistibleAnnotation()) == null)
		    continue; // already packaged into container.
		long annotationMask = annotation.resolvedType != null
			? annotation.resolvedType.getAnnotationTagBits() & TagBits.AnnotationTargetMASK
			: 0;
		if (annotationMask != 0 && (annotationMask & targetMask) == 0) {
		    if (!jdk16packageInfoAnnotation(annotationMask, targetMask))
			continue;
		}
		if (annotation.isRuntimeInvisible() || annotation.isRuntimeTypeInvisible()) {
		    int currentAnnotationOffset = this.contentsOffset;
		    generateAnnotation(annotation, currentAnnotationOffset);
		    invisibleAnnotationsCounter--;
		    if (this.contentsOffset != currentAnnotationOffset) {
			counter++;
		    }
		}
	    }
	    if (counter != 0) {
		this.contents[annotationsLengthOffset++] = (byte) (counter &gt;&gt; 8);
		this.contents[annotationsLengthOffset++] = (byte) counter;

		int attributeLength = this.contentsOffset - attributeLengthOffset - 4;
		this.contents[attributeLengthOffset++] = (byte) (attributeLength &gt;&gt; 24);
		this.contents[attributeLengthOffset++] = (byte) (attributeLength &gt;&gt; 16);
		this.contents[attributeLengthOffset++] = (byte) (attributeLength &gt;&gt; 8);
		this.contents[attributeLengthOffset++] = (byte) attributeLength;
		attributesNumber++;
	    } else {
		this.contentsOffset = annotationAttributeOffset;
	    }
	}

	annotationAttributeOffset = this.contentsOffset;
	if (visibleAnnotationsCounter != 0) {
	    if (this.contentsOffset + 10 &gt;= this.contents.length) {
		resizeContents(10);
	    }
	    int runtimeVisibleAnnotationsAttributeNameIndex = this.constantPool
		    .literalIndex(AttributeNamesConstants.RuntimeVisibleAnnotationsName);
	    this.contents[this.contentsOffset++] = (byte) (runtimeVisibleAnnotationsAttributeNameIndex &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) runtimeVisibleAnnotationsAttributeNameIndex;
	    int attributeLengthOffset = this.contentsOffset;
	    this.contentsOffset += 4; // leave space for the attribute length

	    int annotationsLengthOffset = this.contentsOffset;
	    this.contentsOffset += 2; // leave space for the annotations length

	    int counter = 0;
	    loop: for (int i = 0; i &lt; length; i++) {
		if (visibleAnnotationsCounter == 0)
		    break loop;
		Annotation annotation;
		if ((annotation = annotations[i].getPersistibleAnnotation()) == null)
		    continue; // already packaged into container.
		long annotationMask = annotation.resolvedType != null
			? annotation.resolvedType.getAnnotationTagBits() & TagBits.AnnotationTargetMASK
			: 0;
		if (annotationMask != 0 && (annotationMask & targetMask) == 0) {
		    if (!jdk16packageInfoAnnotation(annotationMask, targetMask))
			continue;
		}
		if (annotation.isRuntimeVisible() || annotation.isRuntimeTypeVisible()) {
		    visibleAnnotationsCounter--;
		    int currentAnnotationOffset = this.contentsOffset;
		    generateAnnotation(annotation, currentAnnotationOffset);
		    if (this.contentsOffset != currentAnnotationOffset) {
			counter++;
		    }
		}
	    }
	    if (counter != 0) {
		this.contents[annotationsLengthOffset++] = (byte) (counter &gt;&gt; 8);
		this.contents[annotationsLengthOffset++] = (byte) counter;

		int attributeLength = this.contentsOffset - attributeLengthOffset - 4;
		this.contents[attributeLengthOffset++] = (byte) (attributeLength &gt;&gt; 24);
		this.contents[attributeLengthOffset++] = (byte) (attributeLength &gt;&gt; 16);
		this.contents[attributeLengthOffset++] = (byte) (attributeLength &gt;&gt; 8);
		this.contents[attributeLengthOffset++] = (byte) attributeLength;
		attributesNumber++;
	    } else {
		this.contentsOffset = annotationAttributeOffset;
	    }
	}
	return attributesNumber;
    }

    /**
     * @param annotationContexts the given annotation contexts
     * @param visibleTypeAnnotationsNumber the given number of visible type annotations
     * @param invisibleTypeAnnotationsNumber the given number of invisible type annotations
     * @return the number of attributes created while dumping the annotations in the .class file
     */
    private int generateRuntimeTypeAnnotations(final AnnotationContext[] annotationContexts,
	    int visibleTypeAnnotationsNumber, int invisibleTypeAnnotationsNumber) {
	int attributesNumber = 0;
	final int length = annotationContexts.length;

	int visibleTypeAnnotationsCounter = visibleTypeAnnotationsNumber;
	int invisibleTypeAnnotationsCounter = invisibleTypeAnnotationsNumber;
	int annotationAttributeOffset = this.contentsOffset;
	if (invisibleTypeAnnotationsCounter != 0) {
	    if (this.contentsOffset + 10 &gt;= this.contents.length) {
		resizeContents(10);
	    }
	    int runtimeInvisibleAnnotationsAttributeNameIndex = this.constantPool
		    .literalIndex(AttributeNamesConstants.RuntimeInvisibleTypeAnnotationsName);
	    this.contents[this.contentsOffset++] = (byte) (runtimeInvisibleAnnotationsAttributeNameIndex &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) runtimeInvisibleAnnotationsAttributeNameIndex;
	    int attributeLengthOffset = this.contentsOffset;
	    this.contentsOffset += 4; // leave space for the attribute length

	    int annotationsLengthOffset = this.contentsOffset;
	    this.contentsOffset += 2; // leave space for the annotations length

	    int counter = 0;
	    loop: for (int i = 0; i &lt; length; i++) {
		if (invisibleTypeAnnotationsCounter == 0)
		    break loop;
		AnnotationContext annotationContext = annotationContexts[i];
		if ((annotationContext.visibility & AnnotationContext.INVISIBLE) != 0) {
		    int currentAnnotationOffset = this.contentsOffset;
		    generateTypeAnnotation(annotationContext, currentAnnotationOffset);
		    invisibleTypeAnnotationsCounter--;
		    if (this.contentsOffset != currentAnnotationOffset) {
			counter++;
		    }
		}
	    }
	    if (counter != 0) {
		this.contents[annotationsLengthOffset++] = (byte) (counter &gt;&gt; 8);
		this.contents[annotationsLengthOffset++] = (byte) counter;

		int attributeLength = this.contentsOffset - attributeLengthOffset - 4;
		this.contents[attributeLengthOffset++] = (byte) (attributeLength &gt;&gt; 24);
		this.contents[attributeLengthOffset++] = (byte) (attributeLength &gt;&gt; 16);
		this.contents[attributeLengthOffset++] = (byte) (attributeLength &gt;&gt; 8);
		this.contents[attributeLengthOffset++] = (byte) attributeLength;
		attributesNumber++;
	    } else {
		this.contentsOffset = annotationAttributeOffset;
	    }
	}

	annotationAttributeOffset = this.contentsOffset;
	if (visibleTypeAnnotationsCounter != 0) {
	    if (this.contentsOffset + 10 &gt;= this.contents.length) {
		resizeContents(10);
	    }
	    int runtimeVisibleAnnotationsAttributeNameIndex = this.constantPool
		    .literalIndex(AttributeNamesConstants.RuntimeVisibleTypeAnnotationsName);
	    this.contents[this.contentsOffset++] = (byte) (runtimeVisibleAnnotationsAttributeNameIndex &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) runtimeVisibleAnnotationsAttributeNameIndex;
	    int attributeLengthOffset = this.contentsOffset;
	    this.contentsOffset += 4; // leave space for the attribute length

	    int annotationsLengthOffset = this.contentsOffset;
	    this.contentsOffset += 2; // leave space for the annotations length

	    int counter = 0;
	    loop: for (int i = 0; i &lt; length; i++) {
		if (visibleTypeAnnotationsCounter == 0)
		    break loop;
		AnnotationContext annotationContext = annotationContexts[i];
		if ((annotationContext.visibility & AnnotationContext.VISIBLE) != 0) {
		    visibleTypeAnnotationsCounter--;
		    int currentAnnotationOffset = this.contentsOffset;
		    generateTypeAnnotation(annotationContext, currentAnnotationOffset);
		    if (this.contentsOffset != currentAnnotationOffset) {
			counter++;
		    }
		}
	    }
	    if (counter != 0) {
		this.contents[annotationsLengthOffset++] = (byte) (counter &gt;&gt; 8);
		this.contents[annotationsLengthOffset++] = (byte) counter;

		int attributeLength = this.contentsOffset - attributeLengthOffset - 4;
		this.contents[attributeLengthOffset++] = (byte) (attributeLength &gt;&gt; 24);
		this.contents[attributeLengthOffset++] = (byte) (attributeLength &gt;&gt; 16);
		this.contents[attributeLengthOffset++] = (byte) (attributeLength &gt;&gt; 8);
		this.contents[attributeLengthOffset++] = (byte) attributeLength;
		attributesNumber++;
	    } else {
		this.contentsOffset = annotationAttributeOffset;
	    }
	}
	return attributesNumber;
    }

    private boolean jdk16packageInfoAnnotation(final long annotationMask, final long targetMask) {
	if (this.targetJDK &lt;= ClassFileConstants.JDK1_6 && targetMask == TagBits.AnnotationForPackage
		&& annotationMask != 0 && (annotationMask & TagBits.AnnotationForPackage) == 0) {
	    return true;
	}
	return false;
    }

    private void generateAnnotation(Annotation annotation, int currentOffset) {
	int startingContentsOffset = currentOffset;
	if (this.contentsOffset + 4 &gt;= this.contents.length) {
	    resizeContents(4);
	}
	TypeBinding annotationTypeBinding = annotation.resolvedType;
	if (annotationTypeBinding == null) {
	    this.contentsOffset = startingContentsOffset;
	    return;
	}
	if (annotationTypeBinding.isMemberType()) {
	    this.recordInnerClasses(annotationTypeBinding);
	}
	final int typeIndex = this.constantPool.literalIndex(annotationTypeBinding.signature());
	this.contents[this.contentsOffset++] = (byte) (typeIndex &gt;&gt; 8);
	this.contents[this.contentsOffset++] = (byte) typeIndex;
	if (annotation instanceof NormalAnnotation) {
	    NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
	    MemberValuePair[] memberValuePairs = normalAnnotation.memberValuePairs;
	    int memberValuePairOffset = this.contentsOffset;
	    if (memberValuePairs != null) {
		int memberValuePairsCount = 0;
		int memberValuePairsLengthPosition = this.contentsOffset;
		this.contentsOffset += 2; // leave space to fill in the pair count later
		int resetPosition = this.contentsOffset;
		final int memberValuePairsLength = memberValuePairs.length;
		loop: for (int i = 0; i &lt; memberValuePairsLength; i++) {
		    MemberValuePair memberValuePair = memberValuePairs[i];
		    if (this.contentsOffset + 2 &gt;= this.contents.length) {
			resizeContents(2);
		    }
		    final int elementNameIndex = this.constantPool.literalIndex(memberValuePair.name);
		    this.contents[this.contentsOffset++] = (byte) (elementNameIndex &gt;&gt; 8);
		    this.contents[this.contentsOffset++] = (byte) elementNameIndex;
		    MethodBinding methodBinding = memberValuePair.binding;
		    if (methodBinding == null) {
			this.contentsOffset = resetPosition;
		    } else {
			try {
			    generateElementValue(memberValuePair.value, methodBinding.returnType,
				    memberValuePairOffset);
			    if (this.contentsOffset == memberValuePairOffset) {
				// ignore all annotation values
				this.contents[this.contentsOffset++] = 0;
				this.contents[this.contentsOffset++] = 0;
				break loop;
			    }
			    memberValuePairsCount++;
			    resetPosition = this.contentsOffset;
			} catch (ClassCastException e) {
			    this.contentsOffset = resetPosition;
			} catch (ShouldNotImplement e) {
			    this.contentsOffset = resetPosition;
			}
		    }
		}
		this.contents[memberValuePairsLengthPosition++] = (byte) (memberValuePairsCount &gt;&gt; 8);
		this.contents[memberValuePairsLengthPosition++] = (byte) memberValuePairsCount;
	    } else {
		this.contents[this.contentsOffset++] = 0;
		this.contents[this.contentsOffset++] = 0;
	    }
	} else if (annotation instanceof SingleMemberAnnotation) {
	    SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) annotation;
	    // this is a single member annotation (one member value)
	    this.contents[this.contentsOffset++] = 0;
	    this.contents[this.contentsOffset++] = 1;
	    if (this.contentsOffset + 2 &gt;= this.contents.length) {
		resizeContents(2);
	    }
	    final int elementNameIndex = this.constantPool.literalIndex(VALUE);
	    this.contents[this.contentsOffset++] = (byte) (elementNameIndex &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) elementNameIndex;
	    MethodBinding methodBinding = singleMemberAnnotation.memberValuePairs()[0].binding;
	    if (methodBinding == null) {
		this.contentsOffset = startingContentsOffset;
	    } else {
		int memberValuePairOffset = this.contentsOffset;
		try {
		    generateElementValue(singleMemberAnnotation.memberValue, methodBinding.returnType,
			    memberValuePairOffset);
		    if (this.contentsOffset == memberValuePairOffset) {
			// completely remove the annotation as its value is invalid
			this.contentsOffset = startingContentsOffset;
		    }
		} catch (ClassCastException e) {
		    this.contentsOffset = startingContentsOffset;
		} catch (ShouldNotImplement e) {
		    this.contentsOffset = startingContentsOffset;
		}
	    }
	} else {
	    // this is a marker annotation (no member value pairs)
	    this.contents[this.contentsOffset++] = 0;
	    this.contents[this.contentsOffset++] = 0;
	}
    }

    private void generateTypeAnnotation(AnnotationContext annotationContext, int currentOffset) {
	Annotation annotation = annotationContext.annotation.getPersistibleAnnotation();
	if (annotation == null || annotation.resolvedType == null)
	    return;

	int targetType = annotationContext.targetType;

	int[] locations = Annotation.getLocations(annotationContext.typeReference, annotationContext.annotation);

	if (this.contentsOffset + 5 &gt;= this.contents.length) {
	    resizeContents(5);
	}
	this.contents[this.contentsOffset++] = (byte) targetType;
	dumpTargetTypeContents(targetType, annotationContext);
	dumpLocations(locations);
	generateAnnotation(annotation, currentOffset);
    }

    public void recordInnerClasses(TypeBinding binding) {
	recordInnerClasses(binding, false);
    }

    private void generateElementValue(Expression defaultValue, TypeBinding memberValuePairReturnType,
	    int attributeOffset) {
	Constant constant = defaultValue.constant;
	TypeBinding defaultValueBinding = defaultValue.resolvedType;
	if (defaultValueBinding == null) {
	    this.contentsOffset = attributeOffset;
	} else {
	    if (defaultValueBinding.isMemberType()) {
		this.recordInnerClasses(defaultValueBinding);
	    }
	    if (memberValuePairReturnType.isMemberType()) {
		this.recordInnerClasses(memberValuePairReturnType);
	    }
	    if (memberValuePairReturnType.isArrayType() && !defaultValueBinding.isArrayType()) {
		// automatic wrapping
		if (this.contentsOffset + 3 &gt;= this.contents.length) {
		    resizeContents(3);
		}
		this.contents[this.contentsOffset++] = (byte) '[';
		this.contents[this.contentsOffset++] = (byte) 0;
		this.contents[this.contentsOffset++] = (byte) 1;
	    }
	    if (constant != null && constant != Constant.NotAConstant) {
		generateElementValue(attributeOffset, defaultValue, constant,
			memberValuePairReturnType.leafComponentType());
	    } else {
		generateElementValueForNonConstantExpression(defaultValue, attributeOffset, defaultValueBinding);
	    }
	}
    }

    private void dumpTargetTypeContents(int targetType, AnnotationContext annotationContext) {
	switch (targetType) {
	case AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER:
	case AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER:
	    // parameter index
	    this.contents[this.contentsOffset++] = (byte) annotationContext.info;
	    break;

	case AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER_BOUND:
	    // type_parameter_index
	    this.contents[this.contentsOffset++] = (byte) annotationContext.info;
	    // bound_index
	    this.contents[this.contentsOffset++] = (byte) annotationContext.info2;
	    break;
	case AnnotationTargetTypeConstants.FIELD:
	case AnnotationTargetTypeConstants.METHOD_RECEIVER:
	case AnnotationTargetTypeConstants.METHOD_RETURN:
	    // target_info is empty_target
	    break;
	case AnnotationTargetTypeConstants.METHOD_FORMAL_PARAMETER:
	    // target_info is parameter index
	    this.contents[this.contentsOffset++] = (byte) annotationContext.info;
	    break;

	case AnnotationTargetTypeConstants.INSTANCEOF:
	case AnnotationTargetTypeConstants.NEW:
	case AnnotationTargetTypeConstants.EXCEPTION_PARAMETER:
	case AnnotationTargetTypeConstants.CONSTRUCTOR_REFERENCE:
	case AnnotationTargetTypeConstants.METHOD_REFERENCE:
	    // bytecode offset for new/instanceof/method_reference
	    // exception table entry index for exception_parameter
	    this.contents[this.contentsOffset++] = (byte) (annotationContext.info &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) annotationContext.info;
	    break;
	case AnnotationTargetTypeConstants.CAST:
	    // bytecode offset
	    this.contents[this.contentsOffset++] = (byte) (annotationContext.info &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) annotationContext.info;
	    this.contents[this.contentsOffset++] = (byte) annotationContext.info2;
	    break;

	case AnnotationTargetTypeConstants.CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT:
	case AnnotationTargetTypeConstants.METHOD_INVOCATION_TYPE_ARGUMENT:
	case AnnotationTargetTypeConstants.CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT:
	case AnnotationTargetTypeConstants.METHOD_REFERENCE_TYPE_ARGUMENT:
	    // bytecode offset
	    this.contents[this.contentsOffset++] = (byte) (annotationContext.info &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) annotationContext.info;
	    // type_argument_index 
	    this.contents[this.contentsOffset++] = (byte) annotationContext.info2;
	    break;

	case AnnotationTargetTypeConstants.CLASS_EXTENDS:
	case AnnotationTargetTypeConstants.THROWS:
	    // For CLASS_EXTENDS - info is supertype index (-1 = superclass)
	    // For THROWS - info is exception table index
	    this.contents[this.contentsOffset++] = (byte) (annotationContext.info &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) annotationContext.info;
	    break;

	case AnnotationTargetTypeConstants.LOCAL_VARIABLE:
	case AnnotationTargetTypeConstants.RESOURCE_VARIABLE:
	    int localVariableTableOffset = this.contentsOffset;
	    LocalVariableBinding localVariable = annotationContext.variableBinding;
	    int actualSize = 0;
	    int initializationCount = localVariable.initializationCount;
	    actualSize += 2 /* for number of entries */ + (6 * initializationCount);
	    // reserve enough space
	    if (this.contentsOffset + actualSize &gt;= this.contents.length) {
		resizeContents(actualSize);
	    }
	    this.contentsOffset += 2;
	    int numberOfEntries = 0;
	    for (int j = 0; j &lt; initializationCount; j++) {
		int startPC = localVariable.initializationPCs[j &lt;&lt; 1];
		int endPC = localVariable.initializationPCs[(j &lt;&lt; 1) + 1];
		if (startPC != endPC) { // only entries for non zero length
		    // now we can safely add the local entry
		    numberOfEntries++;
		    this.contents[this.contentsOffset++] = (byte) (startPC &gt;&gt; 8);
		    this.contents[this.contentsOffset++] = (byte) startPC;
		    int length = endPC - startPC;
		    this.contents[this.contentsOffset++] = (byte) (length &gt;&gt; 8);
		    this.contents[this.contentsOffset++] = (byte) length;
		    int resolvedPosition = localVariable.resolvedPosition;
		    this.contents[this.contentsOffset++] = (byte) (resolvedPosition &gt;&gt; 8);
		    this.contents[this.contentsOffset++] = (byte) resolvedPosition;
		}
	    }
	    this.contents[localVariableTableOffset++] = (byte) (numberOfEntries &gt;&gt; 8);
	    this.contents[localVariableTableOffset] = (byte) numberOfEntries;
	    break;
	case AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER_BOUND:
	    this.contents[this.contentsOffset++] = (byte) annotationContext.info;
	    this.contents[this.contentsOffset++] = (byte) annotationContext.info2;
	    break;
	}
    }

    private void dumpLocations(int[] locations) {
	if (locations == null) {
	    // no type path
	    if (this.contentsOffset + 1 &gt;= this.contents.length) {
		resizeContents(1);
	    }
	    this.contents[this.contentsOffset++] = (byte) 0;
	} else {
	    int length = locations.length;
	    if (this.contentsOffset + length &gt;= this.contents.length) {
		resizeContents(length + 1);
	    }
	    this.contents[this.contentsOffset++] = (byte) (locations.length / 2);
	    for (int i = 0; i &lt; length; i++) {
		this.contents[this.contentsOffset++] = (byte) locations[i];
	    }
	}
    }

    public void recordInnerClasses(TypeBinding binding, boolean onBottomForBug445231) {
	if (this.innerClassesBindings == null) {
	    this.innerClassesBindings = new HashMap(INNER_CLASSES_SIZE);
	}
	ReferenceBinding innerClass = (ReferenceBinding) binding;
	this.innerClassesBindings.put(innerClass.erasure().unannotated(), onBottomForBug445231); // should not emit yet another inner class for Outer.@Inner Inner.
	ReferenceBinding enclosingType = innerClass.enclosingType();
	while (enclosingType != null && enclosingType.isNestedType()) {
	    this.innerClassesBindings.put(enclosingType.erasure().unannotated(), onBottomForBug445231);
	    enclosingType = enclosingType.enclosingType();
	}
    }

    /**
     * @param attributeOffset
     */
    private void generateElementValue(int attributeOffset, Expression defaultValue, Constant constant,
	    TypeBinding binding) {
	if (this.contentsOffset + 3 &gt;= this.contents.length) {
	    resizeContents(3);
	}
	switch (binding.id) {
	case T_boolean:
	    this.contents[this.contentsOffset++] = (byte) 'Z';
	    int booleanValueIndex = this.constantPool.literalIndex(constant.booleanValue() ? 1 : 0);
	    this.contents[this.contentsOffset++] = (byte) (booleanValueIndex &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) booleanValueIndex;
	    break;
	case T_byte:
	    this.contents[this.contentsOffset++] = (byte) 'B';
	    int integerValueIndex = this.constantPool.literalIndex(constant.intValue());
	    this.contents[this.contentsOffset++] = (byte) (integerValueIndex &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) integerValueIndex;
	    break;
	case T_char:
	    this.contents[this.contentsOffset++] = (byte) 'C';
	    integerValueIndex = this.constantPool.literalIndex(constant.intValue());
	    this.contents[this.contentsOffset++] = (byte) (integerValueIndex &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) integerValueIndex;
	    break;
	case T_int:
	    this.contents[this.contentsOffset++] = (byte) 'I';
	    integerValueIndex = this.constantPool.literalIndex(constant.intValue());
	    this.contents[this.contentsOffset++] = (byte) (integerValueIndex &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) integerValueIndex;
	    break;
	case T_short:
	    this.contents[this.contentsOffset++] = (byte) 'S';
	    integerValueIndex = this.constantPool.literalIndex(constant.intValue());
	    this.contents[this.contentsOffset++] = (byte) (integerValueIndex &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) integerValueIndex;
	    break;
	case T_float:
	    this.contents[this.contentsOffset++] = (byte) 'F';
	    int floatValueIndex = this.constantPool.literalIndex(constant.floatValue());
	    this.contents[this.contentsOffset++] = (byte) (floatValueIndex &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) floatValueIndex;
	    break;
	case T_double:
	    this.contents[this.contentsOffset++] = (byte) 'D';
	    int doubleValueIndex = this.constantPool.literalIndex(constant.doubleValue());
	    this.contents[this.contentsOffset++] = (byte) (doubleValueIndex &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) doubleValueIndex;
	    break;
	case T_long:
	    this.contents[this.contentsOffset++] = (byte) 'J';
	    int longValueIndex = this.constantPool.literalIndex(constant.longValue());
	    this.contents[this.contentsOffset++] = (byte) (longValueIndex &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) longValueIndex;
	    break;
	case T_JavaLangString:
	    this.contents[this.contentsOffset++] = (byte) 's';
	    int stringValueIndex = this.constantPool
		    .literalIndex(((StringConstant) constant).stringValue().toCharArray());
	    if (stringValueIndex == -1) {
		if (!this.creatingProblemType) {
		    // report an error and abort: will lead to a problem type classfile creation
		    TypeDeclaration typeDeclaration = this.referenceBinding.scope.referenceContext;
		    typeDeclaration.scope.problemReporter().stringConstantIsExceedingUtf8Limit(defaultValue);
		} else {
		    // already inside a problem type creation : no attribute
		    this.contentsOffset = attributeOffset;
		}
	    } else {
		this.contents[this.contentsOffset++] = (byte) (stringValueIndex &gt;&gt; 8);
		this.contents[this.contentsOffset++] = (byte) stringValueIndex;
	    }
	}
    }

    private void generateElementValueForNonConstantExpression(Expression defaultValue, int attributeOffset,
	    TypeBinding defaultValueBinding) {
	if (defaultValueBinding != null) {
	    if (defaultValueBinding.isEnum()) {
		if (this.contentsOffset + 5 &gt;= this.contents.length) {
		    resizeContents(5);
		}
		this.contents[this.contentsOffset++] = (byte) 'e';
		FieldBinding fieldBinding = null;
		if (defaultValue instanceof QualifiedNameReference) {
		    QualifiedNameReference nameReference = (QualifiedNameReference) defaultValue;
		    fieldBinding = (FieldBinding) nameReference.binding;
		} else if (defaultValue instanceof SingleNameReference) {
		    SingleNameReference nameReference = (SingleNameReference) defaultValue;
		    fieldBinding = (FieldBinding) nameReference.binding;
		} else {
		    this.contentsOffset = attributeOffset;
		}
		if (fieldBinding != null) {
		    final int enumConstantTypeNameIndex = this.constantPool.literalIndex(fieldBinding.type.signature());
		    final int enumConstantNameIndex = this.constantPool.literalIndex(fieldBinding.name);
		    this.contents[this.contentsOffset++] = (byte) (enumConstantTypeNameIndex &gt;&gt; 8);
		    this.contents[this.contentsOffset++] = (byte) enumConstantTypeNameIndex;
		    this.contents[this.contentsOffset++] = (byte) (enumConstantNameIndex &gt;&gt; 8);
		    this.contents[this.contentsOffset++] = (byte) enumConstantNameIndex;
		}
	    } else if (defaultValueBinding.isAnnotationType()) {
		if (this.contentsOffset + 1 &gt;= this.contents.length) {
		    resizeContents(1);
		}
		this.contents[this.contentsOffset++] = (byte) '@';
		generateAnnotation((Annotation) defaultValue, attributeOffset);
	    } else if (defaultValueBinding.isArrayType()) {
		// array type
		if (this.contentsOffset + 3 &gt;= this.contents.length) {
		    resizeContents(3);
		}
		this.contents[this.contentsOffset++] = (byte) '[';
		if (defaultValue instanceof ArrayInitializer) {
		    ArrayInitializer arrayInitializer = (ArrayInitializer) defaultValue;
		    int arrayLength = arrayInitializer.expressions != null ? arrayInitializer.expressions.length : 0;
		    this.contents[this.contentsOffset++] = (byte) (arrayLength &gt;&gt; 8);
		    this.contents[this.contentsOffset++] = (byte) arrayLength;
		    for (int i = 0; i &lt; arrayLength; i++) {
			generateElementValue(arrayInitializer.expressions[i], defaultValueBinding.leafComponentType(),
				attributeOffset);
		    }
		} else {
		    this.contentsOffset = attributeOffset;
		}
	    } else {
		// class type
		if (this.contentsOffset + 3 &gt;= this.contents.length) {
		    resizeContents(3);
		}
		this.contents[this.contentsOffset++] = (byte) 'c';
		if (defaultValue instanceof ClassLiteralAccess) {
		    ClassLiteralAccess classLiteralAccess = (ClassLiteralAccess) defaultValue;
		    final int classInfoIndex = this.constantPool
			    .literalIndex(classLiteralAccess.targetType.signature());
		    this.contents[this.contentsOffset++] = (byte) (classInfoIndex &gt;&gt; 8);
		    this.contents[this.contentsOffset++] = (byte) classInfoIndex;
		} else {
		    this.contentsOffset = attributeOffset;
		}
	    }
	} else {
	    this.contentsOffset = attributeOffset;
	}
    }

}

