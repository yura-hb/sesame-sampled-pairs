import static jdk.internal.org.objectweb.asm.Opcodes.GOTO;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.nashorn.internal.codegen.types.Type;
import jdk.nashorn.internal.runtime.Context;
import jdk.nashorn.internal.runtime.logging.DebugLogger;

class MethodEmitter {
    /**
     * Unconditional jump to a label
     *
     * @param label destination label
     */
    void _goto(final Label label) {
	debug("goto", label);
	jump(GOTO, label, 0);
	doesNotContinueSequentially(); //whoever reaches the point after us provides the stack, because we don't
    }

    private final boolean debug;
    /** The ASM MethodVisitor we are plugged into */
    private final MethodVisitor method;
    /** Current type stack for current evaluation */
    private Label.Stack stack;
    private static int linePrefix = 0;
    /** The context */
    private final Context context;
    /** Debug flag, should we dump all generated bytecode along with stacks? */
    private final DebugLogger log;
    /** dump stack on a particular line, or -1 if disabled */
    private static final int DEBUG_TRACE_LINE;

    private void debug(final Object arg0, final Object arg1) {
	if (debug) {
	    debug(30, new Object[] { arg0, arg1 });
	}
    }

    /**
     * Helper function for jumps, conditional or not
     * @param opcode  opcode for jump
     * @param label   destination
     * @param n       elements on stack to compare, 0-2
     */
    private void jump(final int opcode, final Label label, final int n) {
	for (int i = 0; i &lt; n; i++) {
	    assert peekType().isInteger() || peekType().isBoolean()
		    || peekType().isObject() : "expecting integer type or object for jump, but found " + peekType();
	    popType();
	}
	joinTo(label);
	method.visitJumpInsn(opcode, label.getLabel());
    }

    private void doesNotContinueSequentially() {
	stack = null;
    }

    private void debug(final int padConstant, final Object... args) {
	if (debug) {
	    final StringBuilder sb = new StringBuilder();
	    int pad;

	    sb.append('#');
	    sb.append(++linePrefix);

	    pad = 5 - sb.length();
	    while (pad &gt; 0) {
		sb.append(' ');
		pad--;
	    }

	    if (isReachable() && !stack.isEmpty()) {
		sb.append("{");
		sb.append(stack.size());
		sb.append(":");
		for (int pos = 0; pos &lt; stack.size(); pos++) {
		    final Type t = stack.peek(pos);

		    if (t == Type.SCOPE) {
			sb.append("scope");
		    } else if (t == Type.THIS) {
			sb.append("this");
		    } else if (t.isObject()) {
			String desc = t.getDescriptor();
			int i;
			for (i = 0; desc.charAt(i) == '[' && i &lt; desc.length(); i++) {
			    sb.append('[');
			}
			desc = desc.substring(i);
			final int slash = desc.lastIndexOf('/');
			if (slash != -1) {
			    desc = desc.substring(slash + 1, desc.length() - 1);
			}
			if ("Object".equals(desc)) {
			    sb.append('O');
			} else {
			    sb.append(desc);
			}
		    } else {
			sb.append(t.getDescriptor());
		    }
		    final int loadIndex = stack.localLoads[stack.sp - 1 - pos];
		    if (loadIndex != Label.Stack.NON_LOAD) {
			sb.append('(').append(loadIndex).append(')');
		    }
		    if (pos + 1 &lt; stack.size()) {
			sb.append(' ');
		    }
		}
		sb.append('}');
		sb.append(' ');
	    }

	    pad = padConstant - sb.length();
	    while (pad &gt; 0) {
		sb.append(' ');
		pad--;
	    }

	    for (final Object arg : args) {
		sb.append(arg);
		sb.append(' ');
	    }

	    if (context.getEnv() != null) { //early bootstrap code doesn't have inited context yet
		log.info(sb);
		if (DEBUG_TRACE_LINE == linePrefix) {
		    new Throwable().printStackTrace(log.getOutputStream());
		}
	    }
	}
    }

    /**
     * Peek at the type at the top of the stack
     *
     * @return the type at the top of the stack
     */
    final Type peekType() {
	return stack.peek();
    }

    /**
     * Pop a type from the existing stack, no matter what it is.
     *
     * @return the type
     */
    private Type popType() {
	return stack.pop();
    }

    /**
     * A join in control flow - helper function that makes sure all entry stacks
     * discovered for the join point so far are equivalent
     *
     * MergeStack: we are about to enter a label. If its stack, label.getStack() is null
     * we have never been here before. Then we are expected to carry a stack with us.
     *
     * @param label label
     */
    private void joinTo(final Label label) {
	assert isReachable();
	label.joinFrom(stack);
    }

    boolean isReachable() {
	return stack != null;
    }

}
