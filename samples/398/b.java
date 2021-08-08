import java.io.*;
import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.util.*;
import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Kinds.Kind.*;

class ClassWriter extends ClassFile {
    /** Write "inner classes" attribute.
     */
    void writeInnerClasses() {
	int alenIdx = writeAttr(names.InnerClasses);
	databuf.appendChar(innerClassesQueue.length());
	for (List&lt;ClassSymbol&gt; l = innerClassesQueue.toList(); l.nonEmpty(); l = l.tail) {
	    ClassSymbol inner = l.head;
	    inner.markAbstractIfNeeded(types);
	    char flags = (char) adjustFlags(inner.flags_field);
	    if ((flags & INTERFACE) != 0)
		flags |= ABSTRACT; // Interfaces are always ABSTRACT
	    flags &= ~STRICTFP; //inner classes should not have the strictfp flag set.
	    if (dumpInnerClassModifiers) {
		PrintWriter pw = log.getWriter(Log.WriterKind.ERROR);
		pw.println("INNERCLASS  " + inner.name);
		pw.println("---" + flagNames(flags));
	    }
	    databuf.appendChar(pool.get(inner));
	    databuf.appendChar(inner.owner.kind == TYP && !inner.name.isEmpty() ? pool.get(inner.owner) : 0);
	    databuf.appendChar(!inner.name.isEmpty() ? pool.get(inner.name) : 0);
	    databuf.appendChar(flags);
	}
	endAttr(alenIdx);
    }

    /** The name table. */
    private final Names names;
    /** An output buffer for member info.
     */
    ByteBuffer databuf = new ByteBuffer(DATA_BUF_SIZE);
    /** The inner classes to be written, as a queue where
     *  enclosing classes come first.
     */
    ListBuffer&lt;ClassSymbol&gt; innerClassesQueue;
    /** Type utilities. */
    private Types types;
    private boolean dumpInnerClassModifiers;
    /** The log to use for verbose output.
     */
    private final Log log;
    /** The constant pool.
     */
    Pool pool;
    private final static String[] flagName = { "PUBLIC", "PRIVATE", "PROTECTED", "STATIC", "FINAL", "SUPER", "VOLATILE",
	    "TRANSIENT", "NATIVE", "INTERFACE", "ABSTRACT", "STRICTFP" };

    /** Write header for an attribute to data buffer and return
     *  position past attribute length index.
     */
    int writeAttr(Name attrName) {
	databuf.appendChar(pool.put(attrName));
	databuf.appendInt(0);
	return databuf.length;
    }

    int adjustFlags(final long flags) {
	int result = (int) flags;

	if ((flags & BRIDGE) != 0)
	    result |= ACC_BRIDGE;
	if ((flags & VARARGS) != 0)
	    result |= ACC_VARARGS;
	if ((flags & DEFAULT) != 0)
	    result &= ~ABSTRACT;
	return result;
    }

    /** Return flags as a string, separated by " ".
     */
    public static String flagNames(long flags) {
	StringBuilder sbuf = new StringBuilder();
	int i = 0;
	long f = flags & StandardFlags;
	while (f != 0) {
	    if ((f & 1) != 0) {
		sbuf.append(" ");
		sbuf.append(flagName[i]);
	    }
	    f = f &gt;&gt; 1;
	    i++;
	}
	return sbuf.toString();
    }

    /** Fill in attribute length.
     */
    void endAttr(int index) {
	putInt(databuf, index - 4, databuf.length - index);
    }

    /** Write an integer into given byte buffer;
     *  byte buffer will not be grown.
     */
    void putInt(ByteBuffer buf, int adr, int x) {
	buf.elems[adr] = (byte) ((x &gt;&gt; 24) & 0xFF);
	buf.elems[adr + 1] = (byte) ((x &gt;&gt; 16) & 0xFF);
	buf.elems[adr + 2] = (byte) ((x &gt;&gt; 8) & 0xFF);
	buf.elems[adr + 3] = (byte) ((x) & 0xFF);
    }

}

