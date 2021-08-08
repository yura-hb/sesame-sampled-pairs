import static jdk.internal.org.objectweb.asm.Opcodes.POP;
import static jdk.internal.org.objectweb.asm.Opcodes.POP2;
import jdk.internal.org.objectweb.asm.MethodVisitor;

abstract class Type implements Comparable&lt;Type&gt;, BytecodeOps, Serializable {
    /**
     * Superclass logic for pop for all types
     *
     * @param method method emitter
     * @param type   type to pop
     */
    protected static void pop(final MethodVisitor method, final Type type) {
	method.visitInsn(type.isCategory2() ? POP2 : POP);
    }

    /** How many bytecode slots does this type occupy */
    private transient final int slots;

    /**
     * Determines if a type takes up two bytecode slots or not
     *
     * @return true if type takes up two bytecode slots rather than one
     */
    public boolean isCategory2() {
	return getSlots() == 2;
    }

    /**
     * Determine the number of bytecode slots a type takes up
     *
     * @return the number of slots for this type, 1 or 2.
     */
    public int getSlots() {
	return slots;
    }

}

