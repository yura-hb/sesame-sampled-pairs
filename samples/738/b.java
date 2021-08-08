import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.util.*;
import static com.sun.tools.javac.code.TypeTag.BOT;
import static com.sun.tools.javac.code.TypeTag.INT;
import static com.sun.tools.javac.jvm.ByteCodes.*;
import static com.sun.tools.javac.jvm.UninitializedType.*;
import static com.sun.tools.javac.jvm.ClassWriter.StackMapTableFrame;

class Code {
    /** Emit an opcode with a one-byte operand field;
     *  widen if field does not fit in a byte.
     */
    public void emitop1w(int op, int od) {
	if (od &gt; 0xFF) {
	    emitop(wide);
	    emitop(op);
	    emit2(od);
	} else {
	    emitop(op);
	    emit1(od);
	}
	if (!alive)
	    return;
	switch (op) {
	case iload:
	    state.push(syms.intType);
	    break;
	case lload:
	    state.push(syms.longType);
	    break;
	case fload:
	    state.push(syms.floatType);
	    break;
	case dload:
	    state.push(syms.doubleType);
	    break;
	case aload:
	    state.push(lvar[od].sym.type);
	    break;
	case lstore:
	case dstore:
	    state.pop(2);
	    break;
	case istore:
	case fstore:
	case astore:
	    state.pop(1);
	    break;
	case ret:
	    markDead();
	    break;
	default:
	    throw new AssertionError(mnem(op));
	}
	postop();
    }

    /** Code generation enabled?
     */
    private boolean alive = true;
    /** The current machine state (registers and stack).
     */
    State state;
    final Symtab syms;
    /** Local variables, indexed by register. */
    LocalVar[] lvar;
    /** A chain for jumps to be resolved before the next opcode is emitted.
     *  We do this lazily to avoid jumps to jumps.
     */
    Chain pendingJumps = null;
    /** The position of the currently statement, if we are at the
     *  start of this statement, NOPOS otherwise.
     *  We need this to emit line numbers lazily, which we need to do
     *  because of jump-to-jump optimization.
     */
    int pendingStatPos = Position.NOPOS;
    /** Set true when a stackMap is needed at the current PC. */
    boolean pendingStackMap = false;
    public final boolean debugCode;
    /** the current code pointer.
     */
    public int cp = 0;
    /** The code buffer.
     */
    public byte[] code = new byte[64];
    /** The maximum stack size.
     */
    public int max_stack = 0;
    /** Switch: emit line number info.
     */
    boolean lineDebugInfo;
    /** Emit line number info if map supplied
     */
    Position.LineMap lineMap;
    public final boolean needStackMap;
    /** The stack map format to be generated. */
    StackMapFormat stackMap;
    /** Are we generating code with jumps &ge; 32K?
     */
    public boolean fatcode;
    /** Is it forbidden to compactify code, because something is
     *  pointing to current location?
     */
    private boolean fixedPc = false;
    /** Switch: emit variable debug info.
     */
    boolean varDebugInfo;
    /** A buffer for line number information. Each entry is a vector
     *  of two unsigned shorts.
     */
    List&lt;char[]&gt; lineInfo = List.nil();
    /** The maximum number of local variable slots.
     */
    public int max_locals = 0;
    final Types types;
    /** The last PC at which we generated a stack map. */
    int lastStackMapPC = -1;
    /** A buffer of cldc stack map entries. */
    StackMapFrame[] stackMapBuffer = null;
    int stackMapBufferSize = 0;
    /** The last stack map frame in StackMapTable. */
    StackMapFrame lastFrame = null;
    /** A buffer of compressed StackMapTable entries. */
    StackMapTableFrame[] stackMapTableBuffer = null;
    /** The stack map frame before the last one. */
    StackMapFrame frameBeforeLast = null;
    final MethodSymbol meth;
    /** The next available register.
     */
    public int nextreg = 0;
    /** Previously live local variables, to be put into the variable table. */
    LocalVar[] varBuffer;
    int varBufferSize;

    /** Emit an opcode.
     */
    private void emitop(int op) {
	if (pendingJumps != null)
	    resolvePending();
	if (alive) {
	    if (pendingStatPos != Position.NOPOS)
		markStatBegin();
	    if (pendingStackMap) {
		pendingStackMap = false;
		emitStackMap();
	    }
	    if (debugCode)
		System.err.println("emit@" + cp + " stack=" + state.stacksize + ": " + mnem(op));
	    emit1(op);
	}
    }

    /** Emit two bytes of code.
     */
    private void emit2(int od) {
	if (!alive)
	    return;
	if (cp + 2 &gt; code.length) {
	    emit1(od &gt;&gt; 8);
	    emit1(od);
	} else {
	    code[cp++] = (byte) (od &gt;&gt; 8);
	    code[cp++] = (byte) od;
	}
    }

    /** Emit a byte of code.
     */
    private void emit1(int od) {
	if (!alive)
	    return;
	code = ArrayUtils.ensureCapacity(code, cp);
	code[cp++] = (byte) od;
    }

    /** Switch code generation on/off.
     */
    public void markDead() {
	alive = false;
    }

    /**************************************************************************
    * static tables
    *************************************************************************/

    public static String mnem(int opcode) {
	return Mneumonics.mnem[opcode];
    }

    void postop() {
	Assert.check(alive || state.stacksize == 0);
    }

    /** Resolve any pending jumps.
     */
    public void resolvePending() {
	Chain x = pendingJumps;
	pendingJumps = null;
	resolve(x, cp);
    }

    /** Force stat begin eagerly
     */
    public void markStatBegin() {
	if (alive && lineDebugInfo) {
	    int line = lineMap.getLineNumber(pendingStatPos);
	    char cp1 = (char) cp;
	    char line1 = (char) line;
	    if (cp1 == cp && line1 == line)
		addLineNumber(cp1, line1);
	}
	pendingStatPos = Position.NOPOS;
    }

    /** Emit a stack map entry.  */
    public void emitStackMap() {
	int pc = curCP();
	if (!needStackMap)
	    return;

	switch (stackMap) {
	case CLDC:
	    emitCLDCStackMap(pc, getLocalsSize());
	    break;
	case JSR202:
	    emitStackMapFrame(pc, getLocalsSize());
	    break;
	default:
	    throw new AssertionError("Should have chosen a stackmap format");
	}
	// DEBUG code follows
	if (debugCode)
	    state.dump(pc);
    }

    public static int width(Type type) {
	return type == null ? 1 : width(typecode(type));
    }

    /** Resolve chain to point to given target.
     */
    public void resolve(Chain chain, int target) {
	boolean changed = false;
	State newState = state;
	for (; chain != null; chain = chain.next) {
	    Assert.check(state != chain.state && (target &gt; chain.pc || state.stacksize == 0));
	    if (target &gt;= cp) {
		target = cp;
	    } else if (get1(target) == goto_) {
		if (fatcode)
		    target = target + get4(target + 1);
		else
		    target = target + get2(target + 1);
	    }
	    if (get1(chain.pc) == goto_ && chain.pc + 3 == target && target == cp && !fixedPc) {
		// If goto the next instruction, the jump is not needed:
		// compact the code.
		if (varDebugInfo) {
		    adjustAliveRanges(cp, -3);
		}
		cp = cp - 3;
		target = target - 3;
		if (chain.next == null) {
		    // This is the only jump to the target. Exit the loop
		    // without setting new state. The code is reachable
		    // from the instruction before goto_.
		    alive = true;
		    break;
		}
	    } else {
		if (fatcode)
		    put4(chain.pc + 1, target - chain.pc);
		else if (target - chain.pc &lt; Short.MIN_VALUE || target - chain.pc &gt; Short.MAX_VALUE)
		    fatcode = true;
		else
		    put2(chain.pc + 1, target - chain.pc);
		Assert.check(
			!alive || chain.state.stacksize == newState.stacksize && chain.state.nlocks == newState.nlocks);
	    }
	    fixedPc = true;
	    if (cp == target) {
		changed = true;
		if (debugCode)
		    System.err.println("resolving chain state=" + chain.state);
		if (alive) {
		    newState = chain.state.join(newState);
		} else {
		    newState = chain.state;
		    alive = true;
		}
	    }
	}
	Assert.check(!changed || state != newState);
	if (state != newState) {
	    setDefined(newState.defined);
	    state = newState;
	    pendingStackMap = needStackMap;
	}
    }

    /** Add a line number entry.
     */
    public void addLineNumber(char startPc, char lineNumber) {
	if (lineDebugInfo) {
	    if (lineInfo.nonEmpty() && lineInfo.head[0] == startPc)
		lineInfo = lineInfo.tail;
	    if (lineInfo.isEmpty() || lineInfo.head[1] != lineNumber)
		lineInfo = lineInfo.prepend(new char[] { startPc, lineNumber });
	}
    }

    /** The current output code pointer.
     */
    public int curCP() {
	/*
	 * This method has side-effects because calling it can indirectly provoke
	 *  extra code generation, like goto instructions, depending on the context
	 *  where it's called.
	 *  Use with care or even better avoid using it.
	 */
	if (pendingJumps != null) {
	    resolvePending();
	}
	if (pendingStatPos != Position.NOPOS) {
	    markStatBegin();
	}
	fixedPc = true;
	return cp;
    }

    private int getLocalsSize() {
	int nextLocal = 0;
	for (int i = max_locals - 1; i &gt;= 0; i--) {
	    if (state.defined.isMember(i) && lvar[i] != null) {
		nextLocal = i + width(lvar[i].sym.erasure(types));
		break;
	    }
	}
	return nextLocal;
    }

    /** Emit a CLDC stack map frame. */
    void emitCLDCStackMap(int pc, int localsSize) {
	if (lastStackMapPC == pc) {
	    // drop existing stackmap at this offset
	    stackMapBuffer[--stackMapBufferSize] = null;
	}
	lastStackMapPC = pc;

	if (stackMapBuffer == null) {
	    stackMapBuffer = new StackMapFrame[20];
	} else {
	    stackMapBuffer = ArrayUtils.ensureCapacity(stackMapBuffer, stackMapBufferSize);
	}
	StackMapFrame frame = stackMapBuffer[stackMapBufferSize++] = new StackMapFrame();
	frame.pc = pc;

	frame.locals = new Type[localsSize];
	for (int i = 0; i &lt; localsSize; i++) {
	    if (state.defined.isMember(i) && lvar[i] != null) {
		Type vtype = lvar[i].sym.type;
		if (!(vtype instanceof UninitializedType))
		    vtype = types.erasure(vtype);
		frame.locals[i] = vtype;
	    }
	}
	frame.stack = new Type[state.stacksize];
	for (int i = 0; i &lt; state.stacksize; i++)
	    frame.stack[i] = state.stack[i];
    }

    void emitStackMapFrame(int pc, int localsSize) {
	if (lastFrame == null) {
	    // first frame
	    lastFrame = getInitialFrame();
	} else if (lastFrame.pc == pc) {
	    // drop existing stackmap at this offset
	    stackMapTableBuffer[--stackMapBufferSize] = null;
	    lastFrame = frameBeforeLast;
	    frameBeforeLast = null;
	}

	StackMapFrame frame = new StackMapFrame();
	frame.pc = pc;

	int localCount = 0;
	Type[] locals = new Type[localsSize];
	for (int i = 0; i &lt; localsSize; i++, localCount++) {
	    if (state.defined.isMember(i) && lvar[i] != null) {
		Type vtype = lvar[i].sym.type;
		if (!(vtype instanceof UninitializedType))
		    vtype = types.erasure(vtype);
		locals[i] = vtype;
		if (width(vtype) &gt; 1)
		    i++;
	    }
	}
	frame.locals = new Type[localCount];
	for (int i = 0, j = 0; i &lt; localsSize; i++, j++) {
	    Assert.check(j &lt; localCount);
	    frame.locals[j] = locals[i];
	    if (width(locals[i]) &gt; 1)
		i++;
	}

	int stackCount = 0;
	for (int i = 0; i &lt; state.stacksize; i++) {
	    if (state.stack[i] != null) {
		stackCount++;
	    }
	}
	frame.stack = new Type[stackCount];
	stackCount = 0;
	for (int i = 0; i &lt; state.stacksize; i++) {
	    if (state.stack[i] != null) {
		frame.stack[stackCount++] = types.erasure(state.stack[i]);
	    }
	}

	if (stackMapTableBuffer == null) {
	    stackMapTableBuffer = new StackMapTableFrame[20];
	} else {
	    stackMapTableBuffer = ArrayUtils.ensureCapacity(stackMapTableBuffer, stackMapBufferSize);
	}
	stackMapTableBuffer[stackMapBufferSize++] = StackMapTableFrame.getInstance(frame, lastFrame.pc,
		lastFrame.locals, types);

	frameBeforeLast = lastFrame;
	lastFrame = frame;
    }

    /** Given a type, return its type code (used implicitly in the
     *  JVM architecture).
     */
    public static int typecode(Type type) {
	switch (type.getTag()) {
	case BYTE:
	    return BYTEcode;
	case SHORT:
	    return SHORTcode;
	case CHAR:
	    return CHARcode;
	case INT:
	    return INTcode;
	case LONG:
	    return LONGcode;
	case FLOAT:
	    return FLOATcode;
	case DOUBLE:
	    return DOUBLEcode;
	case BOOLEAN:
	    return BYTEcode;
	case VOID:
	    return VOIDcode;
	case CLASS:
	case ARRAY:
	case METHOD:
	case BOT:
	case TYPEVAR:
	case UNINITIALIZED_THIS:
	case UNINITIALIZED_OBJECT:
	    return OBJECTcode;
	default:
	    throw new AssertionError("typecode " + type.getTag());
	}
    }

    /** The width in bytes of objects of the type.
     */
    public static int width(int typecode) {
	switch (typecode) {
	case LONGcode:
	case DOUBLEcode:
	    return 2;
	case VOIDcode:
	    return 0;
	default:
	    return 1;
	}
    }

    /** Return code byte at position pc as an unsigned int.
     */
    private int get1(int pc) {
	return code[pc] & 0xFF;
    }

    /** Return four code bytes at position pc as an int.
     */
    public int get4(int pc) {
	// pre: pc + 4 &lt;= cp
	return (get1(pc) &lt;&lt; 24) | (get1(pc + 1) &lt;&lt; 16) | (get1(pc + 2) &lt;&lt; 8) | (get1(pc + 3));
    }

    /** Return two code bytes at position pc as an unsigned int.
     */
    private int get2(int pc) {
	return (get1(pc) &lt;&lt; 8) | get1(pc + 1);
    }

    void adjustAliveRanges(int oldCP, int delta) {
	for (LocalVar localVar : lvar) {
	    if (localVar != null) {
		for (LocalVar.Range range : localVar.aliveRanges) {
		    if (range.closed() && range.start_pc + range.length &gt;= oldCP) {
			range.length += delta;
		    }
		}
	    }
	}
    }

    /** Place four  bytes into code at address pc.
     *  Pre: {@literal pc + 4 &lt;= cp }.
     */
    public void put4(int pc, int od) {
	// pre: pc + 4 &lt;= cp
	put1(pc, od &gt;&gt; 24);
	put1(pc + 1, od &gt;&gt; 16);
	put1(pc + 2, od &gt;&gt; 8);
	put1(pc + 3, od);
    }

    /** Place two bytes into code at address pc.
     *  Pre: {@literal pc + 2 &lt;= cp }.
     */
    private void put2(int pc, int od) {
	// pre: pc + 2 &lt;= cp
	put1(pc, od &gt;&gt; 8);
	put1(pc + 1, od);
    }

    /** Set the current variable defined state. */
    public void setDefined(Bits newDefined) {
	if (alive && newDefined != state.defined) {
	    Bits diff = new Bits(state.defined).xorSet(newDefined);
	    for (int adr = diff.nextBit(0); adr &gt;= 0; adr = diff.nextBit(adr + 1)) {
		if (adr &gt;= nextreg)
		    state.defined.excl(adr);
		else if (state.defined.isMember(adr))
		    setUndefined(adr);
		else
		    setDefined(adr);
	    }
	}
    }

    StackMapFrame getInitialFrame() {
	StackMapFrame frame = new StackMapFrame();
	List&lt;Type&gt; arg_types = ((MethodType) meth.externalType(types)).argtypes;
	int len = arg_types.length();
	int count = 0;
	if (!meth.isStatic()) {
	    Type thisType = meth.owner.type;
	    frame.locals = new Type[len + 1];
	    if (meth.isConstructor() && thisType != syms.objectType) {
		frame.locals[count++] = UninitializedType.uninitializedThis(thisType);
	    } else {
		frame.locals[count++] = types.erasure(thisType);
	    }
	} else {
	    frame.locals = new Type[len];
	}
	for (Type arg_type : arg_types) {
	    frame.locals[count++] = types.erasure(arg_type);
	}
	frame.pc = -1;
	frame.stack = null;
	return frame;
    }

    /** Place a byte into code at address pc.
     *  Pre: {@literal pc + 1 &lt;= cp }.
     */
    private void put1(int pc, int op) {
	code[pc] = (byte) op;
    }

    /** Mark a register as being undefined. */
    public void setUndefined(int adr) {
	state.defined.excl(adr);
	if (adr &lt; lvar.length && lvar[adr] != null && lvar[adr].isLastRangeInitialized()) {
	    LocalVar v = lvar[adr];
	    char length = (char) (curCP() - v.lastRange().start_pc);
	    if (length &lt; Character.MAX_VALUE) {
		lvar[adr] = v.dup();
		v.closeRange(length);
		putVar(v);
	    } else {
		v.removeLastRange();
	    }
	}
    }

    /** Mark a register as being (possibly) defined. */
    public void setDefined(int adr) {
	LocalVar v = lvar[adr];
	if (v == null) {
	    state.defined.excl(adr);
	} else {
	    state.defined.incl(adr);
	    if (cp &lt; Character.MAX_VALUE) {
		v.openRange((char) cp);
	    }
	}
    }

    /** Put a live variable range into the buffer to be output to the
     *  class file.
     */
    void putVar(LocalVar var) {
	// Keep local variables if
	// 1) we need them for debug information
	// 2) it is an exception type and it contains type annotations
	boolean keepLocalVariables = varDebugInfo || (var.sym.isExceptionParameter() && var.sym.hasTypeAnnotations());
	if (!keepLocalVariables)
	    return;
	//don't keep synthetic vars, unless they are lambda method parameters
	boolean ignoredSyntheticVar = (var.sym.flags() & Flags.SYNTHETIC) != 0
		&& ((var.sym.owner.flags() & Flags.LAMBDA_METHOD) == 0 || (var.sym.flags() & Flags.PARAMETER) == 0);
	if (ignoredSyntheticVar)
	    return;
	if (varBuffer == null)
	    varBuffer = new LocalVar[20];
	else
	    varBuffer = ArrayUtils.ensureCapacity(varBuffer, varBufferSize);
	varBuffer[varBufferSize++] = var;
    }

    class State implements Cloneable {
	/** Code generation enabled?
	*/
	private boolean alive = true;
	/** The current machine state (registers and stack).
	*/
	State state;
	final Symtab syms;
	/** Local variables, indexed by register. */
	LocalVar[] lvar;
	/** A chain for jumps to be resolved before the next opcode is emitted.
	*  We do this lazily to avoid jumps to jumps.
	*/
	Chain pendingJumps = null;
	/** The position of the currently statement, if we are at the
	*  start of this statement, NOPOS otherwise.
	*  We need this to emit line numbers lazily, which we need to do
	*  because of jump-to-jump optimization.
	*/
	int pendingStatPos = Position.NOPOS;
	/** Set true when a stackMap is needed at the current PC. */
	boolean pendingStackMap = false;
	public final boolean debugCode;
	/** the current code pointer.
	*/
	public int cp = 0;
	/** The code buffer.
	*/
	public byte[] code = new byte[64];
	/** The maximum stack size.
	*/
	public int max_stack = 0;
	/** Switch: emit line number info.
	*/
	boolean lineDebugInfo;
	/** Emit line number info if map supplied
	*/
	Position.LineMap lineMap;
	public final boolean needStackMap;
	/** The stack map format to be generated. */
	StackMapFormat stackMap;
	/** Are we generating code with jumps &ge; 32K?
	*/
	public boolean fatcode;
	/** Is it forbidden to compactify code, because something is
	*  pointing to current location?
	*/
	private boolean fixedPc = false;
	/** Switch: emit variable debug info.
	*/
	boolean varDebugInfo;
	/** A buffer for line number information. Each entry is a vector
	*  of two unsigned shorts.
	*/
	List&lt;char[]&gt; lineInfo = List.nil();
	/** The maximum number of local variable slots.
	*/
	public int max_locals = 0;
	final Types types;
	/** The last PC at which we generated a stack map. */
	int lastStackMapPC = -1;
	/** A buffer of cldc stack map entries. */
	StackMapFrame[] stackMapBuffer = null;
	int stackMapBufferSize = 0;
	/** The last stack map frame in StackMapTable. */
	StackMapFrame lastFrame = null;
	/** A buffer of compressed StackMapTable entries. */
	StackMapTableFrame[] stackMapTableBuffer = null;
	/** The stack map frame before the last one. */
	StackMapFrame frameBeforeLast = null;
	final MethodSymbol meth;
	/** The next available register.
	*/
	public int nextreg = 0;
	/** Previously live local variables, to be put into the variable table. */
	LocalVar[] varBuffer;
	int varBufferSize;

	void push(Type t) {
	    if (debugCode)
		System.err.println("   pushing " + t);
	    switch (t.getTag()) {
	    case VOID:
		return;
	    case BYTE:
	    case CHAR:
	    case SHORT:
	    case BOOLEAN:
		t = syms.intType;
		break;
	    default:
		break;
	    }
	    stack = ArrayUtils.ensureCapacity(stack, stacksize + 2);
	    stack[stacksize++] = t;
	    switch (width(t)) {
	    case 1:
		break;
	    case 2:
		stack[stacksize++] = null;
		break;
	    default:
		throw new AssertionError(t);
	    }
	    if (stacksize &gt; max_stack)
		max_stack = stacksize;
	}

	void pop(int n) {
	    if (debugCode)
		System.err.println("   popping " + n);
	    while (n &gt; 0) {
		stack[--stacksize] = null;
		n--;
	    }
	}

	void dump(int pc) {
	    System.err.print("stackMap for " + meth.owner + "." + meth);
	    if (pc == -1)
		System.out.println();
	    else
		System.out.println(" at " + pc);
	    System.err.println(" stack (from bottom):");
	    for (int i = 0; i &lt; stacksize; i++)
		System.err.println("  " + i + ": " + stack[i]);

	    int lastLocal = 0;
	    for (int i = max_locals - 1; i &gt;= 0; i--) {
		if (defined.isMember(i)) {
		    lastLocal = i;
		    break;
		}
	    }
	    if (lastLocal &gt;= 0)
		System.err.println(" locals:");
	    for (int i = 0; i &lt;= lastLocal; i++) {
		System.err.print("  " + i + ": ");
		if (defined.isMember(i)) {
		    LocalVar var = lvar[i];
		    if (var == null) {
			System.err.println("(none)");
		    } else if (var.sym == null)
			System.err.println("UNKNOWN!");
		    else
			System.err.println("" + var.sym + " of type " + var.sym.erasure(types));
		} else {
		    System.err.println("undefined");
		}
	    }
	    if (nlocks != 0) {
		System.err.print(" locks:");
		for (int i = 0; i &lt; nlocks; i++) {
		    System.err.print(" " + locks[i]);
		}
		System.err.println();
	    }
	}

	State join(State other) {
	    defined.andSet(other.defined);
	    Assert.check(stacksize == other.stacksize && nlocks == other.nlocks);
	    for (int i = 0; i &lt; stacksize;) {
		Type t = stack[i];
		Type tother = other.stack[i];
		Type result = t == tother ? t
			: types.isSubtype(t, tother) ? tother : types.isSubtype(tother, t) ? t : error();
		int w = width(result);
		stack[i] = result;
		if (w == 2)
		    Assert.checkNull(stack[i + 1]);
		i += w;
	    }
	    return this;
	}

	Type error() {
	    throw new AssertionError("inconsistent stack types at join point");
	}

    }

    class LocalVar {
	/** Code generation enabled?
	*/
	private boolean alive = true;
	/** The current machine state (registers and stack).
	*/
	State state;
	final Symtab syms;
	/** Local variables, indexed by register. */
	LocalVar[] lvar;
	/** A chain for jumps to be resolved before the next opcode is emitted.
	*  We do this lazily to avoid jumps to jumps.
	*/
	Chain pendingJumps = null;
	/** The position of the currently statement, if we are at the
	*  start of this statement, NOPOS otherwise.
	*  We need this to emit line numbers lazily, which we need to do
	*  because of jump-to-jump optimization.
	*/
	int pendingStatPos = Position.NOPOS;
	/** Set true when a stackMap is needed at the current PC. */
	boolean pendingStackMap = false;
	public final boolean debugCode;
	/** the current code pointer.
	*/
	public int cp = 0;
	/** The code buffer.
	*/
	public byte[] code = new byte[64];
	/** The maximum stack size.
	*/
	public int max_stack = 0;
	/** Switch: emit line number info.
	*/
	boolean lineDebugInfo;
	/** Emit line number info if map supplied
	*/
	Position.LineMap lineMap;
	public final boolean needStackMap;
	/** The stack map format to be generated. */
	StackMapFormat stackMap;
	/** Are we generating code with jumps &ge; 32K?
	*/
	public boolean fatcode;
	/** Is it forbidden to compactify code, because something is
	*  pointing to current location?
	*/
	private boolean fixedPc = false;
	/** Switch: emit variable debug info.
	*/
	boolean varDebugInfo;
	/** A buffer for line number information. Each entry is a vector
	*  of two unsigned shorts.
	*/
	List&lt;char[]&gt; lineInfo = List.nil();
	/** The maximum number of local variable slots.
	*/
	public int max_locals = 0;
	final Types types;
	/** The last PC at which we generated a stack map. */
	int lastStackMapPC = -1;
	/** A buffer of cldc stack map entries. */
	StackMapFrame[] stackMapBuffer = null;
	int stackMapBufferSize = 0;
	/** The last stack map frame in StackMapTable. */
	StackMapFrame lastFrame = null;
	/** A buffer of compressed StackMapTable entries. */
	StackMapTableFrame[] stackMapTableBuffer = null;
	/** The stack map frame before the last one. */
	StackMapFrame frameBeforeLast = null;
	final MethodSymbol meth;
	/** The next available register.
	*/
	public int nextreg = 0;
	/** Previously live local variables, to be put into the variable table. */
	LocalVar[] varBuffer;
	int varBufferSize;

	public boolean isLastRangeInitialized() {
	    if (aliveRanges.isEmpty()) {
		return false;
	    }
	    return lastRange().start_pc != Character.MAX_VALUE;
	}

	Range lastRange() {
	    return aliveRanges.isEmpty() ? null : aliveRanges.get(aliveRanges.size() - 1);
	}

	public LocalVar dup() {
	    return new LocalVar(sym);
	}

	public void closeRange(char length) {
	    if (isLastRangeInitialized() && length &gt; 0) {
		Range range = lastRange();
		if (range != null) {
		    if (range.length == Character.MAX_VALUE) {
			range.length = length;
		    }
		}
	    } else {
		removeLastRange();
	    }
	}

	void removeLastRange() {
	    Range lastRange = lastRange();
	    if (lastRange != null) {
		aliveRanges.remove(lastRange);
	    }
	}

	public void openRange(char start) {
	    if (!hasOpenRange()) {
		aliveRanges.add(new Range(start));
	    }
	}

	LocalVar(VarSymbol v) {
	    this.sym = v;
	    this.reg = (char) v.adr;
	}

	public boolean hasOpenRange() {
	    if (aliveRanges.isEmpty()) {
		return false;
	    }
	    return lastRange().length == Character.MAX_VALUE;
	}

	class Range {
	    final VarSymbol sym;
	    java.util.List&lt;Range&gt; aliveRanges = new java.util.ArrayList&lt;&gt;();
	    final char reg;

	    boolean closed() {
		return start_pc != Character.MAX_VALUE && length != Character.MAX_VALUE;
	    }

	    Range(char start) {
		this.start_pc = start;
	    }

	}

    }

}

