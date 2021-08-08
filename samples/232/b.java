import com.sun.tools.javac.code.*;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.code.Type.*;
import com.sun.tools.javac.util.*;
import static com.sun.tools.javac.code.Flags.*;

class ClassWriter extends ClassFile {
    /**
     * Write method parameter names attribute.
     */
    int writeMethodParametersAttr(MethodSymbol m) {
	MethodType ty = m.externalType(types).asMethodType();
	final int allparams = ty.argtypes.size();
	if (m.params != null && allparams != 0) {
	    final int attrIndex = writeAttr(names.MethodParameters);
	    databuf.appendByte(allparams);
	    // Write extra parameters first
	    for (VarSymbol s : m.extraParams) {
		final int flags = ((int) s.flags() & (FINAL | SYNTHETIC | MANDATED)) | ((int) m.flags() & SYNTHETIC);
		databuf.appendChar(pool.put(s.name));
		databuf.appendChar(flags);
	    }
	    // Now write the real parameters
	    for (VarSymbol s : m.params) {
		final int flags = ((int) s.flags() & (FINAL | SYNTHETIC | MANDATED)) | ((int) m.flags() & SYNTHETIC);
		databuf.appendChar(pool.put(s.name));
		databuf.appendChar(flags);
	    }
	    // Now write the captured locals
	    for (VarSymbol s : m.capturedLocals) {
		final int flags = ((int) s.flags() & (FINAL | SYNTHETIC | MANDATED)) | ((int) m.flags() & SYNTHETIC);
		databuf.appendChar(pool.put(s.name));
		databuf.appendChar(flags);
	    }
	    endAttr(attrIndex);
	    return 1;
	} else
	    return 0;
    }

    /** Type utilities. */
    private Types types;
    /** The name table. */
    private final Names names;
    /** An output buffer for member info.
     */
    ByteBuffer databuf = new ByteBuffer(DATA_BUF_SIZE);
    /** The constant pool.
     */
    Pool pool;

    /** Write header for an attribute to data buffer and return
     *  position past attribute length index.
     */
    int writeAttr(Name attrName) {
	databuf.appendChar(pool.put(attrName));
	databuf.appendInt(0);
	return databuf.length;
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

