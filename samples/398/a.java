import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.ArrayInitializer;
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FunctionalExpression;
import org.eclipse.jdt.internal.compiler.ast.LambdaExpression;
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
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

class ClassFile implements TypeConstants, TypeIds {
    /**
     * INTERNAL USE-ONLY
     * This methods generate all the attributes for the receiver.
     * For a class they could be:
     * - source file attribute
     * - inner classes attribute
     * - deprecated attribute
     */
    public void addAttributes() {
	// update the method count
	this.contents[this.methodCountOffset++] = (byte) (this.methodCount &gt;&gt; 8);
	this.contents[this.methodCountOffset] = (byte) this.methodCount;

	int attributesNumber = 0;
	// leave two bytes for the number of attributes and store the current offset
	int attributeOffset = this.contentsOffset;
	this.contentsOffset += 2;

	// source attribute
	if ((this.produceAttributes & ClassFileConstants.ATTR_SOURCE) != 0) {
	    String fullFileName = new String(this.referenceBinding.scope.referenceCompilationUnit().getFileName());
	    fullFileName = fullFileName.replace('\\', '/');
	    int lastIndex = fullFileName.lastIndexOf('/');
	    if (lastIndex != -1) {
		fullFileName = fullFileName.substring(lastIndex + 1, fullFileName.length());
	    }
	    attributesNumber += generateSourceAttribute(fullFileName);
	}
	// Deprecated attribute
	if (this.referenceBinding.isDeprecated()) {
	    // check that there is enough space to write all the bytes for the field info corresponding
	    // to the @fieldBinding
	    attributesNumber += generateDeprecatedAttribute();
	}
	// add signature attribute
	char[] genericSignature = this.referenceBinding.genericSignature();
	if (genericSignature != null) {
	    attributesNumber += generateSignatureAttribute(genericSignature);
	}
	if (this.targetJDK &gt;= ClassFileConstants.JDK1_5 && this.referenceBinding.isNestedType()
		&& !this.referenceBinding.isMemberType()) {
	    // add enclosing method attribute (1.5 mode only)
	    attributesNumber += generateEnclosingMethodAttribute();
	}
	if (this.targetJDK &gt;= ClassFileConstants.JDK1_4) {
	    TypeDeclaration typeDeclaration = this.referenceBinding.scope.referenceContext;
	    if (typeDeclaration != null) {
		final Annotation[] annotations = typeDeclaration.annotations;
		if (annotations != null) {
		    long targetMask;
		    if (typeDeclaration.isPackageInfo())
			targetMask = TagBits.AnnotationForPackage;
		    else if (this.referenceBinding.isAnnotationType())
			targetMask = TagBits.AnnotationForType | TagBits.AnnotationForAnnotationType;
		    else
			targetMask = TagBits.AnnotationForType | TagBits.AnnotationForTypeUse;
		    attributesNumber += generateRuntimeAnnotations(annotations, targetMask);
		}
	    }
	}

	if (this.referenceBinding.isHierarchyInconsistent()) {
	    ReferenceBinding superclass = this.referenceBinding.superclass;
	    if (superclass != null) {
		this.missingTypes = superclass.collectMissingTypes(this.missingTypes);
	    }
	    ReferenceBinding[] superInterfaces = this.referenceBinding.superInterfaces();
	    for (int i = 0, max = superInterfaces.length; i &lt; max; i++) {
		this.missingTypes = superInterfaces[i].collectMissingTypes(this.missingTypes);
	    }
	    attributesNumber += generateHierarchyInconsistentAttribute();
	}
	// Functional expression and lambda bootstrap methods
	if (this.bootstrapMethods != null && !this.bootstrapMethods.isEmpty()) {
	    attributesNumber += generateBootstrapMethods(this.bootstrapMethods);
	}
	// Inner class attribute
	int numberOfInnerClasses = this.innerClassesBindings == null ? 0 : this.innerClassesBindings.size();
	if (numberOfInnerClasses != 0) {
	    ReferenceBinding[] innerClasses = new ReferenceBinding[numberOfInnerClasses];
	    this.innerClassesBindings.keySet().toArray(innerClasses);
	    Arrays.sort(innerClasses, new Comparator() {
		@Override
		public int compare(Object o1, Object o2) {
		    TypeBinding binding1 = (TypeBinding) o1;
		    TypeBinding binding2 = (TypeBinding) o2;
		    Boolean onBottom1 = ClassFile.this.innerClassesBindings.get(o1);
		    Boolean onBottom2 = ClassFile.this.innerClassesBindings.get(o2);
		    if (onBottom1) {
			if (!onBottom2) {
			    return 1;
			}
		    } else {
			if (onBottom2) {
			    return -1;
			}
		    }
		    return CharOperation.compareTo(binding1.constantPoolName(), binding2.constantPoolName());
		}
	    });
	    attributesNumber += generateInnerClassAttribute(numberOfInnerClasses, innerClasses);
	}
	if (this.missingTypes != null) {
	    generateMissingTypesAttribute();
	    attributesNumber++;
	}

	attributesNumber += generateTypeAnnotationAttributeForTypeDeclaration();

	if (this.targetJDK &gt;= ClassFileConstants.JDK11) {
	    // add nestMember and nestHost attributes
	    attributesNumber += generateNestAttributes();
	}
	// update the number of attributes
	if (attributeOffset + 2 &gt;= this.contents.length) {
	    resizeContents(2);
	}
	this.contents[attributeOffset++] = (byte) (attributesNumber &gt;&gt; 8);
	this.contents[attributeOffset] = (byte) attributesNumber;

	// resynchronize all offsets of the classfile
	this.header = this.constantPool.poolContent;
	this.headerOffset = this.constantPool.currentOffset;
	int constantPoolCount = this.constantPool.currentIndex;
	this.header[this.constantPoolOffset++] = (byte) (constantPoolCount &gt;&gt; 8);
	this.header[this.constantPoolOffset] = (byte) constantPoolCount;
    }

    public byte[] contents;
    public int methodCountOffset;
    public int methodCount;
    public int contentsOffset;
    public int produceAttributes;
    public SourceTypeBinding referenceBinding;
    public long targetJDK;
    public List&lt;TypeBinding&gt; missingTypes = null;
    public List bootstrapMethods = null;
    public Map&lt;TypeBinding, Boolean&gt; innerClassesBindings;
    public byte[] header;
    public ConstantPool constantPool;
    public int headerOffset;
    public int constantPoolOffset;
    public static final int INNER_CLASSES_SIZE = 5;
    protected boolean creatingProblemType;

    private int generateSourceAttribute(String fullFileName) {
	int localContentsOffset = this.contentsOffset;
	// check that there is enough space to write all the bytes for the field info corresponding
	// to the @fieldBinding
	if (localContentsOffset + 8 &gt;= this.contents.length) {
	    resizeContents(8);
	}
	int sourceAttributeNameIndex = this.constantPool.literalIndex(AttributeNamesConstants.SourceName);
	this.contents[localContentsOffset++] = (byte) (sourceAttributeNameIndex &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) sourceAttributeNameIndex;
	// The length of a source file attribute is 2. This is a fixed-length
	// attribute
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 2;
	// write the source file name
	int fileNameIndex = this.constantPool.literalIndex(fullFileName.toCharArray());
	this.contents[localContentsOffset++] = (byte) (fileNameIndex &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) fileNameIndex;
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

    private int generateEnclosingMethodAttribute() {
	int localContentsOffset = this.contentsOffset;
	// add enclosing method attribute (1.5 mode only)
	if (localContentsOffset + 10 &gt;= this.contents.length) {
	    resizeContents(10);
	}
	int enclosingMethodAttributeNameIndex = this.constantPool
		.literalIndex(AttributeNamesConstants.EnclosingMethodName);
	this.contents[localContentsOffset++] = (byte) (enclosingMethodAttributeNameIndex &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) enclosingMethodAttributeNameIndex;
	// the length of a signature attribute is equals to 2
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 4;

	int enclosingTypeIndex = this.constantPool
		.literalIndexForType(this.referenceBinding.enclosingType().constantPoolName());
	this.contents[localContentsOffset++] = (byte) (enclosingTypeIndex &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) enclosingTypeIndex;
	byte methodIndexByte1 = 0;
	byte methodIndexByte2 = 0;
	if (this.referenceBinding instanceof LocalTypeBinding) {
	    MethodBinding methodBinding = ((LocalTypeBinding) this.referenceBinding).enclosingMethod;
	    if (methodBinding != null) {
		int enclosingMethodIndex = this.constantPool.literalIndexForNameAndType(methodBinding.selector,
			methodBinding.signature(this));
		methodIndexByte1 = (byte) (enclosingMethodIndex &gt;&gt; 8);
		methodIndexByte2 = (byte) enclosingMethodIndex;
	    }
	}
	this.contents[localContentsOffset++] = methodIndexByte1;
	this.contents[localContentsOffset++] = methodIndexByte2;
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

    private int generateHierarchyInconsistentAttribute() {
	int localContentsOffset = this.contentsOffset;
	// add an attribute for inconsistent hierarchy
	if (localContentsOffset + 6 &gt;= this.contents.length) {
	    resizeContents(6);
	}
	int inconsistentHierarchyNameIndex = this.constantPool
		.literalIndex(AttributeNamesConstants.InconsistentHierarchy);
	this.contents[localContentsOffset++] = (byte) (inconsistentHierarchyNameIndex &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) inconsistentHierarchyNameIndex;
	// the length of an inconsistent hierarchy attribute is equals to 0
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contentsOffset = localContentsOffset;
	return 1;
    }

    private int generateBootstrapMethods(List functionalExpressionList) {
	/* See JVM spec 4.7.21
	   The BootstrapMethods attribute has the following format:
	   BootstrapMethods_attribute {
	      u2 attribute_name_index;
	      u4 attribute_length;
	      u2 num_bootstrap_methods;
	      {   u2 bootstrap_method_ref;
	          u2 num_bootstrap_arguments;
	          u2 bootstrap_arguments[num_bootstrap_arguments];
	      } bootstrap_methods[num_bootstrap_methods];
	 }
	*/
	// Record inner classes for MethodHandles$Lookup
	ReferenceBinding methodHandlesLookup = this.referenceBinding.scope.getJavaLangInvokeMethodHandlesLookup();
	if (methodHandlesLookup == null)
	    return 0; // skip bootstrap section, class path problem already reported, just avoid NPE.
	recordInnerClasses(methodHandlesLookup); // Should be done, it's what javac does also
	ReferenceBinding javaLangInvokeLambdaMetafactory = this.referenceBinding.scope
		.getJavaLangInvokeLambdaMetafactory();

	// Depending on the complexity of the expression it may be necessary to use the altMetafactory() rather than the metafactory()
	int indexForMetaFactory = 0;
	int indexForAltMetaFactory = 0;

	int numberOfBootstraps = functionalExpressionList.size();
	int localContentsOffset = this.contentsOffset;
	// Generate the boot strap attribute - since we are only making lambdas and
	// functional expressions, we know the size ahead of time - this less general
	// than the full invokedynamic scope, but fine for Java 8

	final int contentsEntries = 10;
	int exSize = contentsEntries * numberOfBootstraps + 8;
	if (exSize + localContentsOffset &gt;= this.contents.length) {
	    resizeContents(exSize);
	}

	int attributeNameIndex = this.constantPool.literalIndex(AttributeNamesConstants.BootstrapMethodsName);
	this.contents[localContentsOffset++] = (byte) (attributeNameIndex &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) attributeNameIndex;
	// leave space for attribute_length and remember where to insert it
	int attributeLengthPosition = localContentsOffset;
	localContentsOffset += 4;
	this.contents[localContentsOffset++] = (byte) (numberOfBootstraps &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) numberOfBootstraps;
	for (int i = 0; i &lt; numberOfBootstraps; i++) {
	    FunctionalExpression functional = (FunctionalExpression) functionalExpressionList.get(i);
	    MethodBinding[] bridges = functional.getRequiredBridges();
	    TypeBinding[] markerInterfaces = null;
	    if ((functional instanceof LambdaExpression
		    && (((markerInterfaces = ((LambdaExpression) functional).getMarkerInterfaces()) != null))
		    || bridges != null) || functional.isSerializable) {
		// may need even more space
		int extraSpace = 2; // at least 2 more than when the normal metafactory is used, for the bitflags entry
		if (markerInterfaces != null) {
		    // 2 for the marker interface list size then 2 per marker interface index
		    extraSpace += (2 + 2 * markerInterfaces.length);
		}
		if (bridges != null) {
		    // 2 for bridge count then 2 per bridge method type.
		    extraSpace += (2 + 2 * bridges.length);
		}
		if (extraSpace + contentsEntries + localContentsOffset &gt;= this.contents.length) {
		    resizeContents(extraSpace + contentsEntries);
		}

		if (indexForAltMetaFactory == 0) {
		    indexForAltMetaFactory = this.constantPool.literalIndexForMethodHandle(
			    ClassFileConstants.MethodHandleRefKindInvokeStatic, javaLangInvokeLambdaMetafactory,
			    ConstantPool.ALTMETAFACTORY,
			    ConstantPool.JAVA_LANG_INVOKE_LAMBDAMETAFACTORY_ALTMETAFACTORY_SIGNATURE, false);
		}
		this.contents[localContentsOffset++] = (byte) (indexForAltMetaFactory &gt;&gt; 8);
		this.contents[localContentsOffset++] = (byte) indexForAltMetaFactory;

		// u2 num_bootstrap_arguments
		this.contents[localContentsOffset++] = 0;
		this.contents[localContentsOffset++] = (byte) (4
			+ (markerInterfaces == null ? 0 : 1 + markerInterfaces.length)
			+ (bridges == null ? 0 : 1 + bridges.length));

		int functionalDescriptorIndex = this.constantPool
			.literalIndexForMethodType(functional.descriptor.original().signature());
		this.contents[localContentsOffset++] = (byte) (functionalDescriptorIndex &gt;&gt; 8);
		this.contents[localContentsOffset++] = (byte) functionalDescriptorIndex;

		int methodHandleIndex = this.constantPool.literalIndexForMethodHandle(functional.binding.original()); // Speak of " implementation" (erased) version here, adaptations described below.
		this.contents[localContentsOffset++] = (byte) (methodHandleIndex &gt;&gt; 8);
		this.contents[localContentsOffset++] = (byte) methodHandleIndex;

		char[] instantiatedSignature = functional.descriptor.signature();
		int methodTypeIndex = this.constantPool.literalIndexForMethodType(instantiatedSignature);
		this.contents[localContentsOffset++] = (byte) (methodTypeIndex &gt;&gt; 8);
		this.contents[localContentsOffset++] = (byte) methodTypeIndex;

		int bitflags = 0;
		if (functional.isSerializable) {
		    bitflags |= ClassFileConstants.FLAG_SERIALIZABLE;
		}
		if (markerInterfaces != null) {
		    bitflags |= ClassFileConstants.FLAG_MARKERS;
		}
		if (bridges != null) {
		    bitflags |= ClassFileConstants.FLAG_BRIDGES;
		}
		int indexForBitflags = this.constantPool.literalIndex(bitflags);

		this.contents[localContentsOffset++] = (byte) (indexForBitflags &gt;&gt; 8);
		this.contents[localContentsOffset++] = (byte) (indexForBitflags);

		if (markerInterfaces != null) {
		    int markerInterfaceCountIndex = this.constantPool.literalIndex(markerInterfaces.length);
		    this.contents[localContentsOffset++] = (byte) (markerInterfaceCountIndex &gt;&gt; 8);
		    this.contents[localContentsOffset++] = (byte) (markerInterfaceCountIndex);
		    for (int m = 0, maxm = markerInterfaces.length; m &lt; maxm; m++) {
			int classTypeIndex = this.constantPool.literalIndexForType(markerInterfaces[m]);
			this.contents[localContentsOffset++] = (byte) (classTypeIndex &gt;&gt; 8);
			this.contents[localContentsOffset++] = (byte) (classTypeIndex);
		    }
		}
		if (bridges != null) {
		    int bridgeCountIndex = this.constantPool.literalIndex(bridges.length);
		    this.contents[localContentsOffset++] = (byte) (bridgeCountIndex &gt;&gt; 8);
		    this.contents[localContentsOffset++] = (byte) (bridgeCountIndex);
		    for (int m = 0, maxm = bridges.length; m &lt; maxm; m++) {
			char[] bridgeSignature = bridges[m].signature();
			int bridgeMethodTypeIndex = this.constantPool.literalIndexForMethodType(bridgeSignature);
			this.contents[localContentsOffset++] = (byte) (bridgeMethodTypeIndex &gt;&gt; 8);
			this.contents[localContentsOffset++] = (byte) bridgeMethodTypeIndex;
		    }
		}
	    } else {
		if (contentsEntries + localContentsOffset &gt;= this.contents.length) {
		    resizeContents(contentsEntries);
		}
		if (indexForMetaFactory == 0) {
		    indexForMetaFactory = this.constantPool.literalIndexForMethodHandle(
			    ClassFileConstants.MethodHandleRefKindInvokeStatic, javaLangInvokeLambdaMetafactory,
			    ConstantPool.METAFACTORY,
			    ConstantPool.JAVA_LANG_INVOKE_LAMBDAMETAFACTORY_METAFACTORY_SIGNATURE, false);
		}
		this.contents[localContentsOffset++] = (byte) (indexForMetaFactory &gt;&gt; 8);
		this.contents[localContentsOffset++] = (byte) indexForMetaFactory;

		// u2 num_bootstrap_arguments
		this.contents[localContentsOffset++] = 0;
		this.contents[localContentsOffset++] = (byte) 3;

		int functionalDescriptorIndex = this.constantPool
			.literalIndexForMethodType(functional.descriptor.original().signature());
		this.contents[localContentsOffset++] = (byte) (functionalDescriptorIndex &gt;&gt; 8);
		this.contents[localContentsOffset++] = (byte) functionalDescriptorIndex;

		int methodHandleIndex = this.constantPool.literalIndexForMethodHandle(
			functional.binding instanceof PolymorphicMethodBinding ? functional.binding
				: functional.binding.original()); // Speak of " implementation" (erased) version here, adaptations described below.
		this.contents[localContentsOffset++] = (byte) (methodHandleIndex &gt;&gt; 8);
		this.contents[localContentsOffset++] = (byte) methodHandleIndex;

		char[] instantiatedSignature = functional.descriptor.signature();
		int methodTypeIndex = this.constantPool.literalIndexForMethodType(instantiatedSignature);
		this.contents[localContentsOffset++] = (byte) (methodTypeIndex &gt;&gt; 8);
		this.contents[localContentsOffset++] = (byte) methodTypeIndex;
	    }
	}

	int attributeLength = localContentsOffset - attributeLengthPosition - 4;
	this.contents[attributeLengthPosition++] = (byte) (attributeLength &gt;&gt; 24);
	this.contents[attributeLengthPosition++] = (byte) (attributeLength &gt;&gt; 16);
	this.contents[attributeLengthPosition++] = (byte) (attributeLength &gt;&gt; 8);
	this.contents[attributeLengthPosition++] = (byte) attributeLength;
	this.contentsOffset = localContentsOffset;
	return 1;
    }

    private int generateInnerClassAttribute(int numberOfInnerClasses, ReferenceBinding[] innerClasses) {
	int localContentsOffset = this.contentsOffset;
	// Generate the inner class attribute
	int exSize = 8 * numberOfInnerClasses + 8;
	if (exSize + localContentsOffset &gt;= this.contents.length) {
	    resizeContents(exSize);
	}
	// Now we now the size of the attribute and the number of entries
	// attribute name
	int attributeNameIndex = this.constantPool.literalIndex(AttributeNamesConstants.InnerClassName);
	this.contents[localContentsOffset++] = (byte) (attributeNameIndex &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) attributeNameIndex;
	int value = (numberOfInnerClasses &lt;&lt; 3) + 2;
	this.contents[localContentsOffset++] = (byte) (value &gt;&gt; 24);
	this.contents[localContentsOffset++] = (byte) (value &gt;&gt; 16);
	this.contents[localContentsOffset++] = (byte) (value &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) value;
	this.contents[localContentsOffset++] = (byte) (numberOfInnerClasses &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) numberOfInnerClasses;
	for (int i = 0; i &lt; numberOfInnerClasses; i++) {
	    ReferenceBinding innerClass = innerClasses[i];
	    int accessFlags = innerClass.getAccessFlags();
	    int innerClassIndex = this.constantPool.literalIndexForType(innerClass.constantPoolName());
	    // inner class index
	    this.contents[localContentsOffset++] = (byte) (innerClassIndex &gt;&gt; 8);
	    this.contents[localContentsOffset++] = (byte) innerClassIndex;
	    // outer class index: anonymous and local have no outer class index
	    if (innerClass.isMemberType()) {
		// member or member of local
		int outerClassIndex = this.constantPool
			.literalIndexForType(innerClass.enclosingType().constantPoolName());
		this.contents[localContentsOffset++] = (byte) (outerClassIndex &gt;&gt; 8);
		this.contents[localContentsOffset++] = (byte) outerClassIndex;
	    } else {
		// equals to 0 if the innerClass is not a member type
		this.contents[localContentsOffset++] = 0;
		this.contents[localContentsOffset++] = 0;
	    }
	    // name index
	    if (!innerClass.isAnonymousType()) {
		int nameIndex = this.constantPool.literalIndex(innerClass.sourceName());
		this.contents[localContentsOffset++] = (byte) (nameIndex &gt;&gt; 8);
		this.contents[localContentsOffset++] = (byte) nameIndex;
	    } else {
		// equals to 0 if the innerClass is an anonymous type
		this.contents[localContentsOffset++] = 0;
		this.contents[localContentsOffset++] = 0;
	    }
	    // access flag
	    if (innerClass.isAnonymousType()) {
		accessFlags &= ~ClassFileConstants.AccFinal;
	    } else if (innerClass.isMemberType() && innerClass.isInterface()) {
		accessFlags |= ClassFileConstants.AccStatic; // implicitely static
	    }
	    this.contents[localContentsOffset++] = (byte) (accessFlags &gt;&gt; 8);
	    this.contents[localContentsOffset++] = (byte) accessFlags;
	}
	this.contentsOffset = localContentsOffset;
	return 1;
    }

    private void generateMissingTypesAttribute() {
	int initialSize = this.missingTypes.size();
	int[] missingTypesIndexes = new int[initialSize];
	int numberOfMissingTypes = 0;
	if (initialSize &gt; 1) {
	    Collections.sort(this.missingTypes, new Comparator() {
		@Override
		public int compare(Object o1, Object o2) {
		    TypeBinding typeBinding1 = (TypeBinding) o1;
		    TypeBinding typeBinding2 = (TypeBinding) o2;
		    return CharOperation.compareTo(typeBinding1.constantPoolName(), typeBinding2.constantPoolName());
		}
	    });
	}
	int previousIndex = 0;
	next: for (int i = 0; i &lt; initialSize; i++) {
	    int missingTypeIndex = this.constantPool.literalIndexForType(this.missingTypes.get(i));
	    if (previousIndex == missingTypeIndex) {
		continue next;
	    }
	    previousIndex = missingTypeIndex;
	    missingTypesIndexes[numberOfMissingTypes++] = missingTypeIndex;
	}
	// we don't need to resize as we interate from 0 to numberOfMissingTypes when recording the indexes in the .class file
	int attributeLength = numberOfMissingTypes * 2 + 2;
	if (this.contentsOffset + attributeLength + 6 &gt;= this.contents.length) {
	    resizeContents(attributeLength + 6);
	}
	int missingTypesNameIndex = this.constantPool.literalIndex(AttributeNamesConstants.MissingTypesName);
	this.contents[this.contentsOffset++] = (byte) (missingTypesNameIndex &gt;&gt; 8);
	this.contents[this.contentsOffset++] = (byte) missingTypesNameIndex;

	// generate attribute length
	this.contents[this.contentsOffset++] = (byte) (attributeLength &gt;&gt; 24);
	this.contents[this.contentsOffset++] = (byte) (attributeLength &gt;&gt; 16);
	this.contents[this.contentsOffset++] = (byte) (attributeLength &gt;&gt; 8);
	this.contents[this.contentsOffset++] = (byte) attributeLength;

	// generate number of missing types
	this.contents[this.contentsOffset++] = (byte) (numberOfMissingTypes &gt;&gt; 8);
	this.contents[this.contentsOffset++] = (byte) numberOfMissingTypes;
	// generate entry for each missing type
	for (int i = 0; i &lt; numberOfMissingTypes; i++) {
	    int missingTypeIndex = missingTypesIndexes[i];
	    this.contents[this.contentsOffset++] = (byte) (missingTypeIndex &gt;&gt; 8);
	    this.contents[this.contentsOffset++] = (byte) missingTypeIndex;
	}
    }

    private int generateTypeAnnotationAttributeForTypeDeclaration() {
	TypeDeclaration typeDeclaration = this.referenceBinding.scope.referenceContext;
	if ((typeDeclaration.bits & ASTNode.HasTypeAnnotations) == 0) {
	    return 0;
	}
	int attributesNumber = 0;
	int visibleTypeAnnotationsCounter = 0;
	int invisibleTypeAnnotationsCounter = 0;
	TypeReference superclass = typeDeclaration.superclass;
	List allTypeAnnotationContexts = new ArrayList();
	if (superclass != null && (superclass.bits & ASTNode.HasTypeAnnotations) != 0) {
	    superclass.getAllAnnotationContexts(AnnotationTargetTypeConstants.CLASS_EXTENDS, -1,
		    allTypeAnnotationContexts);
	}
	TypeReference[] superInterfaces = typeDeclaration.superInterfaces;
	if (superInterfaces != null) {
	    for (int i = 0; i &lt; superInterfaces.length; i++) {
		TypeReference superInterface = superInterfaces[i];
		if ((superInterface.bits & ASTNode.HasTypeAnnotations) == 0) {
		    continue;
		}
		superInterface.getAllAnnotationContexts(AnnotationTargetTypeConstants.CLASS_EXTENDS, i,
			allTypeAnnotationContexts);
	    }
	}
	TypeParameter[] typeParameters = typeDeclaration.typeParameters;
	if (typeParameters != null) {
	    for (int i = 0, max = typeParameters.length; i &lt; max; i++) {
		TypeParameter typeParameter = typeParameters[i];
		if ((typeParameter.bits & ASTNode.HasTypeAnnotations) != 0) {
		    typeParameter.getAllAnnotationContexts(AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER, i,
			    allTypeAnnotationContexts);
		}
	    }
	}
	int size = allTypeAnnotationContexts.size();
	if (size != 0) {
	    AnnotationContext[] allTypeAnnotationContextsArray = new AnnotationContext[size];
	    allTypeAnnotationContexts.toArray(allTypeAnnotationContextsArray);
	    for (int j = 0, max = allTypeAnnotationContextsArray.length; j &lt; max; j++) {
		AnnotationContext annotationContext = allTypeAnnotationContextsArray[j];
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
	return attributesNumber;
    }

    private int generateNestAttributes() {
	int nAttrs = generateNestMembersAttribute(); //either member or host will exist 4.7.29
	nAttrs += generateNestHostAttribute();
	return nAttrs;
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

    public void recordInnerClasses(TypeBinding binding) {
	recordInnerClasses(binding, false);
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

    private int generateNestMembersAttribute() {

	int localContentsOffset = this.contentsOffset;
	List&lt;String&gt; nestedMembers = this.referenceBinding.getNestMembers();
	int numberOfNestedMembers = nestedMembers != null ? nestedMembers.size() : 0;
	if (numberOfNestedMembers == 0) // JVMS 11 4.7.29 says "at most one" NestMembers attribute - return if none.
	    return 0;

	int exSize = 8 + 2 * numberOfNestedMembers;
	if (exSize + localContentsOffset &gt;= this.contents.length) {
	    resizeContents(exSize);
	}
	int attributeNameIndex = this.constantPool.literalIndex(AttributeNamesConstants.NestMembers);
	this.contents[localContentsOffset++] = (byte) (attributeNameIndex &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) attributeNameIndex;
	int value = (numberOfNestedMembers &lt;&lt; 1) + 2;
	this.contents[localContentsOffset++] = (byte) (value &gt;&gt; 24);
	this.contents[localContentsOffset++] = (byte) (value &gt;&gt; 16);
	this.contents[localContentsOffset++] = (byte) (value &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) value;
	this.contents[localContentsOffset++] = (byte) (numberOfNestedMembers &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) numberOfNestedMembers;

	for (int i = 0; i &lt; numberOfNestedMembers; i++) {
	    char[] nestMemberName = nestedMembers.get(i).toCharArray();
	    int nestedMemberIndex = this.constantPool.literalIndexForType(nestMemberName);
	    this.contents[localContentsOffset++] = (byte) (nestedMemberIndex &gt;&gt; 8);
	    this.contents[localContentsOffset++] = (byte) nestedMemberIndex;
	}
	this.contentsOffset = localContentsOffset;
	return 1;
    }

    private int generateNestHostAttribute() {
	SourceTypeBinding nestHost = this.referenceBinding.getNestHost();
	if (nestHost == null)
	    return 0;
	int localContentsOffset = this.contentsOffset;
	if (localContentsOffset + 10 &gt;= this.contents.length) {
	    resizeContents(10);
	}
	int nestHostAttributeNameIndex = this.constantPool.literalIndex(AttributeNamesConstants.NestHost);
	this.contents[localContentsOffset++] = (byte) (nestHostAttributeNameIndex &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) nestHostAttributeNameIndex;

	// The value of the attribute_length item must be two.
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 0;
	this.contents[localContentsOffset++] = 2;

	int nestHostIndex = this.constantPool.literalIndexForType(nestHost.constantPoolName());
	this.contents[localContentsOffset++] = (byte) (nestHostIndex &gt;&gt; 8);
	this.contents[localContentsOffset++] = (byte) nestHostIndex;
	this.contentsOffset = localContentsOffset;
	return 1;
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

}

