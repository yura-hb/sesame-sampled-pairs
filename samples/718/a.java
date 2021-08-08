import org.eclipse.jdt.internal.compiler.lookup.*;

class CodeStream {
    /**
    * @param accessBinding the access method binding to generate
    */
    public void generateSyntheticBodyForConstructorAccess(SyntheticMethodBinding accessBinding) {
	initializeMaxLocals(accessBinding);
	MethodBinding constructorBinding = accessBinding.targetMethod;
	TypeBinding[] parameters = constructorBinding.parameters;
	int length = parameters.length;
	int resolvedPosition = 1;
	aload_0();
	// special name&ordinal argument generation for enum constructors
	TypeBinding declaringClass = constructorBinding.declaringClass;
	if (declaringClass.erasure().id == TypeIds.T_JavaLangEnum || declaringClass.isEnum()) {
	    aload_1(); // pass along name param as name arg
	    iload_2(); // pass along ordinal param as ordinal arg
	    resolvedPosition += 2;
	}
	if (declaringClass.isNestedType()) {
	    NestedTypeBinding nestedType = (NestedTypeBinding) declaringClass;
	    SyntheticArgumentBinding[] syntheticArguments = nestedType.syntheticEnclosingInstances();
	    for (int i = 0; i &lt; (syntheticArguments == null ? 0 : syntheticArguments.length); i++) {
		TypeBinding type;
		load((type = syntheticArguments[i].type), resolvedPosition);
		switch (type.id) {
		case TypeIds.T_long:
		case TypeIds.T_double:
		    resolvedPosition += 2;
		    break;
		default:
		    resolvedPosition++;
		    break;
		}
	    }
	}
	for (int i = 0; i &lt; length; i++) {
	    TypeBinding parameter;
	    load(parameter = parameters[i], resolvedPosition);
	    switch (parameter.id) {
	    case TypeIds.T_long:
	    case TypeIds.T_double:
		resolvedPosition += 2;
		break;
	    default:
		resolvedPosition++;
		break;
	    }
	}

	if (declaringClass.isNestedType()) {
	    NestedTypeBinding nestedType = (NestedTypeBinding) declaringClass;
	    SyntheticArgumentBinding[] syntheticArguments = nestedType.syntheticOuterLocalVariables();
	    for (int i = 0; i &lt; (syntheticArguments == null ? 0 : syntheticArguments.length); i++) {
		TypeBinding type;
		load(type = syntheticArguments[i].type, resolvedPosition);
		switch (type.id) {
		case TypeIds.T_long:
		case TypeIds.T_double:
		    resolvedPosition += 2;
		    break;
		default:
		    resolvedPosition++;
		    break;
		}
	    }
	}
	invoke(Opcodes.OPC_invokespecial, constructorBinding, null /* default declaringClass */);
	return_();
    }

    public int maxLocals;
    public int countLabels;
    public int stackDepth;
    public int stackMax;
    public int classFileOffset;
    public byte[] bCodeStream;
    public int position;
    public int lastAbruptCompletion;
    public ClassFile classFile;
    public ConstantPool constantPool;

    /**
    * @param methodBinding the given method binding to initialize the max locals
    */
    public void initializeMaxLocals(MethodBinding methodBinding) {
	if (methodBinding == null) {
	    this.maxLocals = 0;
	    return;
	}
	this.maxLocals = methodBinding.isStatic() ? 0 : 1;
	ReferenceBinding declaringClass = methodBinding.declaringClass;
	// take into account enum constructor synthetic name+ordinal
	if (methodBinding.isConstructor() && declaringClass.isEnum()) {
	    this.maxLocals += 2; // String and int (enum constant name+ordinal)
	}

	// take into account the synthetic parameters
	if (methodBinding.isConstructor() && declaringClass.isNestedType()) {
	    this.maxLocals += declaringClass.getEnclosingInstancesSlotSize();
	    this.maxLocals += declaringClass.getOuterLocalVariablesSlotSize();
	}
	TypeBinding[] parameterTypes;
	if ((parameterTypes = methodBinding.parameters) != null) {
	    for (int i = 0, max = parameterTypes.length; i &lt; max; i++) {
		switch (parameterTypes[i].id) {
		case TypeIds.T_long:
		case TypeIds.T_double:
		    this.maxLocals += 2;
		    break;
		default:
		    this.maxLocals++;
		}
	    }
	}
    }

    public void aload_0() {
	this.countLabels = 0;
	this.stackDepth++;
	if (this.stackDepth &gt; this.stackMax) {
	    this.stackMax = this.stackDepth;
	}
	if (this.maxLocals == 0) {
	    this.maxLocals = 1;
	}
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_aload_0;
    }

    public void aload_1() {
	this.countLabels = 0;
	this.stackDepth++;
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.maxLocals &lt;= 1) {
	    this.maxLocals = 2;
	}
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_aload_1;
    }

    public void iload_2() {
	this.countLabels = 0;
	this.stackDepth++;
	if (this.maxLocals &lt;= 2) {
	    this.maxLocals = 3;
	}
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_iload_2;
    }

    protected final void load(TypeBinding typeBinding, int resolvedPosition) {
	this.countLabels = 0;
	// Using dedicated int bytecode
	switch (typeBinding.id) {
	case TypeIds.T_int:
	case TypeIds.T_byte:
	case TypeIds.T_char:
	case TypeIds.T_boolean:
	case TypeIds.T_short:
	    switch (resolvedPosition) {
	    case 0:
		iload_0();
		break;
	    case 1:
		iload_1();
		break;
	    case 2:
		iload_2();
		break;
	    case 3:
		iload_3();
		break;
	    //case -1 :
	    // internal failure: trying to load variable not supposed to be generated
	    //	break;
	    default:
		iload(resolvedPosition);
	    }
	    break;
	case TypeIds.T_float:
	    switch (resolvedPosition) {
	    case 0:
		fload_0();
		break;
	    case 1:
		fload_1();
		break;
	    case 2:
		fload_2();
		break;
	    case 3:
		fload_3();
		break;
	    default:
		fload(resolvedPosition);
	    }
	    break;
	case TypeIds.T_long:
	    switch (resolvedPosition) {
	    case 0:
		lload_0();
		break;
	    case 1:
		lload_1();
		break;
	    case 2:
		lload_2();
		break;
	    case 3:
		lload_3();
		break;
	    default:
		lload(resolvedPosition);
	    }
	    break;
	case TypeIds.T_double:
	    switch (resolvedPosition) {
	    case 0:
		dload_0();
		break;
	    case 1:
		dload_1();
		break;
	    case 2:
		dload_2();
		break;
	    case 3:
		dload_3();
		break;
	    default:
		dload(resolvedPosition);
	    }
	    break;
	default:
	    switch (resolvedPosition) {
	    case 0:
		aload_0();
		break;
	    case 1:
		aload_1();
		break;
	    case 2:
		aload_2();
		break;
	    case 3:
		aload_3();
		break;
	    default:
		aload(resolvedPosition);
	    }
	}
    }

    public void invoke(byte opcode, MethodBinding methodBinding, TypeBinding declaringClass) {
	this.invoke(opcode, methodBinding, declaringClass, null);
    }

    public void return_() {
	this.countLabels = 0;
	// the stackDepth should be equal to 0
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_return;
	this.lastAbruptCompletion = this.position;
    }

    private final void resizeByteArray() {
	int length = this.bCodeStream.length;
	int requiredSize = length + length;
	if (this.classFileOffset &gt;= requiredSize) {
	    // must be sure to grow enough
	    requiredSize = this.classFileOffset + length;
	}
	System.arraycopy(this.bCodeStream, 0, this.bCodeStream = new byte[requiredSize], 0, length);
    }

    public void iload_0() {
	this.countLabels = 0;
	this.stackDepth++;
	if (this.maxLocals &lt;= 0) {
	    this.maxLocals = 1;
	}
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_iload_0;
    }

    public void iload_1() {
	this.countLabels = 0;
	this.stackDepth++;
	if (this.maxLocals &lt;= 1) {
	    this.maxLocals = 2;
	}
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_iload_1;
    }

    public void iload_3() {
	this.countLabels = 0;
	this.stackDepth++;
	if (this.maxLocals &lt;= 3) {
	    this.maxLocals = 4;
	}
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_iload_3;
    }

    public void iload(int iArg) {
	this.countLabels = 0;
	this.stackDepth++;
	if (this.maxLocals &lt;= iArg) {
	    this.maxLocals = iArg + 1;
	}
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (iArg &gt; 255) { // Widen
	    if (this.classFileOffset + 3 &gt;= this.bCodeStream.length) {
		resizeByteArray();
	    }
	    this.position += 2;
	    this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_wide;
	    this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_iload;
	    writeUnsignedShort(iArg);
	} else {
	    if (this.classFileOffset + 1 &gt;= this.bCodeStream.length) {
		resizeByteArray();
	    }
	    this.position += 2;
	    this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_iload;
	    this.bCodeStream[this.classFileOffset++] = (byte) iArg;
	}
    }

    public void fload_0() {
	this.countLabels = 0;
	this.stackDepth++;
	if (this.maxLocals == 0) {
	    this.maxLocals = 1;
	}
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_fload_0;
    }

    public void fload_1() {
	this.countLabels = 0;
	this.stackDepth++;
	if (this.maxLocals &lt;= 1) {
	    this.maxLocals = 2;
	}
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_fload_1;
    }

    public void fload_2() {
	this.countLabels = 0;
	this.stackDepth++;
	if (this.maxLocals &lt;= 2) {
	    this.maxLocals = 3;
	}
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_fload_2;
    }

    public void fload_3() {
	this.countLabels = 0;
	this.stackDepth++;
	if (this.maxLocals &lt;= 3) {
	    this.maxLocals = 4;
	}
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_fload_3;
    }

    public void fload(int iArg) {
	this.countLabels = 0;
	this.stackDepth++;
	if (this.maxLocals &lt;= iArg) {
	    this.maxLocals = iArg + 1;
	}
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (iArg &gt; 255) { // Widen
	    if (this.classFileOffset + 3 &gt;= this.bCodeStream.length) {
		resizeByteArray();
	    }
	    this.position += 2;
	    this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_wide;
	    this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_fload;
	    writeUnsignedShort(iArg);
	} else {
	    if (this.classFileOffset + 1 &gt;= this.bCodeStream.length) {
		resizeByteArray();
	    }
	    this.position += 2;
	    this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_fload;
	    this.bCodeStream[this.classFileOffset++] = (byte) iArg;
	}
    }

    public void lload_0() {
	this.countLabels = 0;
	this.stackDepth += 2;
	if (this.maxLocals &lt; 2) {
	    this.maxLocals = 2;
	}
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_lload_0;
    }

    public void lload_1() {
	this.countLabels = 0;
	this.stackDepth += 2;
	if (this.maxLocals &lt; 3) {
	    this.maxLocals = 3;
	}
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_lload_1;
    }

    public void lload_2() {
	this.countLabels = 0;
	this.stackDepth += 2;
	if (this.maxLocals &lt; 4) {
	    this.maxLocals = 4;
	}
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_lload_2;
    }

    public void lload_3() {
	this.countLabels = 0;
	this.stackDepth += 2;
	if (this.maxLocals &lt; 5) {
	    this.maxLocals = 5;
	}
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_lload_3;
    }

    public void lload(int iArg) {
	this.countLabels = 0;
	this.stackDepth += 2;
	if (this.maxLocals &lt;= iArg + 1) {
	    this.maxLocals = iArg + 2;
	}
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (iArg &gt; 255) { // Widen
	    if (this.classFileOffset + 3 &gt;= this.bCodeStream.length) {
		resizeByteArray();
	    }
	    this.position += 2;
	    this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_wide;
	    this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_lload;
	    writeUnsignedShort(iArg);
	} else {
	    if (this.classFileOffset + 1 &gt;= this.bCodeStream.length) {
		resizeByteArray();
	    }
	    this.position += 2;
	    this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_lload;
	    this.bCodeStream[this.classFileOffset++] = (byte) iArg;
	}
    }

    public void dload_0() {
	this.countLabels = 0;
	this.stackDepth += 2;
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.maxLocals &lt; 2) {
	    this.maxLocals = 2;
	}
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_dload_0;
    }

    public void dload_1() {
	this.countLabels = 0;
	this.stackDepth += 2;
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.maxLocals &lt; 3) {
	    this.maxLocals = 3;
	}
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_dload_1;
    }

    public void dload_2() {
	this.countLabels = 0;
	this.stackDepth += 2;
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.maxLocals &lt; 4) {
	    this.maxLocals = 4;
	}
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_dload_2;
    }

    public void dload_3() {
	this.countLabels = 0;
	this.stackDepth += 2;
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.maxLocals &lt; 5) {
	    this.maxLocals = 5;
	}
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_dload_3;
    }

    public void dload(int iArg) {
	this.countLabels = 0;
	this.stackDepth += 2;
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.maxLocals &lt; iArg + 2) {
	    this.maxLocals = iArg + 2; // + 2 because it is a double
	}
	if (iArg &gt; 255) { // Widen
	    if (this.classFileOffset + 3 &gt;= this.bCodeStream.length) {
		resizeByteArray();
	    }
	    this.position += 2;
	    this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_wide;
	    this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_dload;
	    writeUnsignedShort(iArg);
	} else {
	    // Don't need to use the wide bytecode
	    if (this.classFileOffset + 1 &gt;= this.bCodeStream.length) {
		resizeByteArray();
	    }
	    this.position += 2;
	    this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_dload;
	    this.bCodeStream[this.classFileOffset++] = (byte) iArg;
	}
    }

    public void aload_2() {
	this.countLabels = 0;
	this.stackDepth++;
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.maxLocals &lt;= 2) {
	    this.maxLocals = 3;
	}
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_aload_2;
    }

    public void aload_3() {
	this.countLabels = 0;
	this.stackDepth++;
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.maxLocals &lt;= 3) {
	    this.maxLocals = 4;
	}
	if (this.classFileOffset &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_aload_3;
    }

    public void aload(int iArg) {
	this.countLabels = 0;
	this.stackDepth++;
	if (this.stackDepth &gt; this.stackMax)
	    this.stackMax = this.stackDepth;
	if (this.maxLocals &lt;= iArg) {
	    this.maxLocals = iArg + 1;
	}
	if (iArg &gt; 255) { // Widen
	    if (this.classFileOffset + 3 &gt;= this.bCodeStream.length) {
		resizeByteArray();
	    }
	    this.position += 2;
	    this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_wide;
	    this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_aload;
	    writeUnsignedShort(iArg);
	} else {
	    // Don't need to use the wide bytecode
	    if (this.classFileOffset + 1 &gt;= this.bCodeStream.length) {
		resizeByteArray();
	    }
	    this.position += 2;
	    this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_aload;
	    this.bCodeStream[this.classFileOffset++] = (byte) iArg;
	}
    }

    public void invoke(byte opcode, MethodBinding methodBinding, TypeBinding declaringClass,
	    TypeReference[] typeArguments) {
	if (declaringClass == null)
	    declaringClass = methodBinding.declaringClass;
	if ((declaringClass.tagBits & TagBits.ContainsNestedTypeReferences) != 0) {
	    Util.recordNestedType(this.classFile, declaringClass);
	}
	// compute receiverAndArgsSize
	int receiverAndArgsSize;
	switch (opcode) {
	case Opcodes.OPC_invokestatic:
	    receiverAndArgsSize = 0; // no receiver
	    break;
	case Opcodes.OPC_invokeinterface:
	case Opcodes.OPC_invokevirtual:
	    receiverAndArgsSize = 1; // receiver
	    break;
	case Opcodes.OPC_invokespecial:
	    receiverAndArgsSize = 1; // receiver
	    if (methodBinding.isConstructor()) {
		if (declaringClass.isNestedType()) {
		    ReferenceBinding nestedType = (ReferenceBinding) declaringClass;
		    // enclosing instances
		    receiverAndArgsSize += nestedType.getEnclosingInstancesSlotSize();
		    // outer local variables
		    SyntheticArgumentBinding[] syntheticArguments = nestedType.syntheticOuterLocalVariables();
		    if (syntheticArguments != null) {
			for (int i = 0, max = syntheticArguments.length; i &lt; max; i++) {
			    switch (syntheticArguments[i].id) {
			    case TypeIds.T_double:
			    case TypeIds.T_long:
				receiverAndArgsSize += 2;
				break;
			    default:
				receiverAndArgsSize++;
				break;
			    }
			}
		    }
		}
		if (declaringClass.isEnum()) {
		    // adding String (name) and int (ordinal)
		    receiverAndArgsSize += 2;
		}
	    }
	    break;
	default:
	    return; // should not occur

	}
	for (int i = methodBinding.parameters.length - 1; i &gt;= 0; i--) {
	    switch (methodBinding.parameters[i].id) {
	    case TypeIds.T_double:
	    case TypeIds.T_long:
		receiverAndArgsSize += 2;
		break;
	    default:
		receiverAndArgsSize++;
		break;
	    }
	}
	// compute return type size
	int returnTypeSize;
	switch (methodBinding.returnType.id) {
	case TypeIds.T_double:
	case TypeIds.T_long:
	    returnTypeSize = 2;
	    break;
	case TypeIds.T_void:
	    returnTypeSize = 0;
	    break;
	default:
	    returnTypeSize = 1;
	    break;
	}
	invoke18(opcode, receiverAndArgsSize, returnTypeSize, declaringClass.constantPoolName(),
		declaringClass.isInterface(), methodBinding.selector, methodBinding.signature(this.classFile));
    }

    /**
    * Write a unsigned 16 bits value into the byte array
    * @param value the unsigned short
    */
    private final void writeUnsignedShort(int value) {
	// no bound check since used only from within codestream where already checked
	this.position += 2;
	this.bCodeStream[this.classFileOffset++] = (byte) (value &gt;&gt;&gt; 8);
	this.bCodeStream[this.classFileOffset++] = (byte) value;
    }

    private void invoke18(byte opcode, int receiverAndArgsSize, int returnTypeSize, char[] declaringClass,
	    boolean isInterface, char[] selector, char[] signature) {
	this.countLabels = 0;
	if (opcode == Opcodes.OPC_invokeinterface) {
	    // invokeinterface
	    if (this.classFileOffset + 4 &gt;= this.bCodeStream.length) {
		resizeByteArray();
	    }
	    this.position += 3;
	    this.bCodeStream[this.classFileOffset++] = opcode;
	    writeUnsignedShort(this.constantPool.literalIndexForMethod(declaringClass, selector, signature, true));
	    this.bCodeStream[this.classFileOffset++] = (byte) receiverAndArgsSize;
	    this.bCodeStream[this.classFileOffset++] = 0;
	} else {
	    // invokespecial
	    // invokestatic
	    // invokevirtual
	    if (this.classFileOffset + 2 &gt;= this.bCodeStream.length) {
		resizeByteArray();
	    }
	    this.position++;
	    this.bCodeStream[this.classFileOffset++] = opcode;
	    writeUnsignedShort(
		    this.constantPool.literalIndexForMethod(declaringClass, selector, signature, isInterface));
	}
	this.stackDepth += returnTypeSize - receiverAndArgsSize;
	if (this.stackDepth &gt; this.stackMax) {
	    this.stackMax = this.stackDepth;
	}
    }

}

