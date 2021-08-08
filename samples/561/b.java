import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

class CodeStream implements OperatorIds, ClassFileConstants, Opcodes, BaseTypes, TypeConstants, TypeIds {
    /**
    * The equivalent code performs a string conversion:
    *
    * @param blockScope the given blockScope
    * @param oper1 the first expression
    * @param oper2 the second expression
    */
    public void generateStringConcatenationAppend(BlockScope blockScope, Expression oper1, Expression oper2) {
	int pc;
	if (oper1 == null) {
	    /* Operand is already on the stack, and maybe nil:
	    note type1 is always to  java.lang.String here.*/
	    this.newStringContatenation();
	    this.dup_x1();
	    this.swap();
	    // If argument is reference type, need to transform it 
	    // into a string (handles null case)
	    this.invokeStringValueOf(T_Object);
	    this.invokeStringConcatenationStringConstructor();
	} else {
	    pc = position;
	    oper1.generateOptimizedStringConcatenationCreation(blockScope, this, oper1.implicitConversion & 0xF);
	    this.recordPositionsFrom(pc, oper1.sourceStart);
	}
	pc = position;
	oper2.generateOptimizedStringConcatenation(blockScope, this, oper2.implicitConversion & 0xF);
	this.recordPositionsFrom(pc, oper2.sourceStart);
	this.invokeStringConcatenationToString();
    }

    public int position;
    public static final boolean DEBUG = false;
    private long targetLevel;
    public int countLabels;
    public int stackDepth;
    public int stackMax;
    public int classFileOffset;
    public byte[] bCodeStream;
    public ConstantPool constantPool;
    public boolean generateLineNumberAttributes;
    public int pcToSourceMapSize;
    public int[] pcToSourceMap = new int[24];
    public int[] lineSeparatorPositions;
    public int lastEntryPC;

    public void newStringContatenation() {
	// new: java.lang.StringBuffer
	// new: java.lang.StringBuilder
	if (DEBUG) {
	    if (this.targetLevel &gt;= JDK1_5) {
		System.out.println(position + "\t\tnew: java.lang.StringBuilder"); //$NON-NLS-1$
	    } else {
		System.out.println(position + "\t\tnew: java.lang.StringBuffer"); //$NON-NLS-1$
	    }
	}
	countLabels = 0;
	stackDepth++;
	if (stackDepth &gt; stackMax) {
	    stackMax = stackDepth;
	}
	if (classFileOffset + 2 &gt;= bCodeStream.length) {
	    resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_new;
	if (this.targetLevel &gt;= JDK1_5) {
	    writeUnsignedShort(constantPool.literalIndexForJavaLangStringBuilder());
	} else {
	    writeUnsignedShort(constantPool.literalIndexForJavaLangStringBuffer());
	}
    }

    final public void dup_x1() {
	if (DEBUG)
	    System.out.println(position + "\t\tdup_x1"); //$NON-NLS-1$
	countLabels = 0;
	stackDepth++;
	if (stackDepth &gt; stackMax)
	    stackMax = stackDepth;
	if (classFileOffset &gt;= bCodeStream.length) {
	    resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_dup_x1;
    }

    final public void swap() {
	if (DEBUG)
	    System.out.println(position + "\t\tswap"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset &gt;= bCodeStream.length) {
	    resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_swap;
    }

    public void invokeStringValueOf(int typeID) {
	// invokestatic: java.lang.String.valueOf(argumentType)
	if (DEBUG)
	    System.out.println(position + "\t\tinvokestatic: java.lang.String.valueOf(...)"); //$NON-NLS-1$
	countLabels = 0;
	if (classFileOffset + 2 &gt;= bCodeStream.length) {
	    resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokestatic;
	writeUnsignedShort(constantPool.literalIndexForJavaLangStringValueOf(typeID));
    }

    public void invokeStringConcatenationStringConstructor() {
	if (DEBUG) {
	    if (this.targetLevel &gt;= JDK1_5) {
		System.out.println(position + "\t\tjava.lang.StringBuilder.&lt;init&gt;(Ljava.lang.String;)V"); //$NON-NLS-1$
	    } else {
		System.out.println(position + "\t\tjava.lang.StringBuffer.&lt;init&gt;(Ljava.lang.String;)V"); //$NON-NLS-1$
	    }
	}
	countLabels = 0;
	if (classFileOffset + 2 &gt;= bCodeStream.length) {
	    resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokespecial;
	if (this.targetLevel &gt;= JDK1_5) {
	    writeUnsignedShort(constantPool.literalIndexForJavaLangStringBuilderConstructor());
	} else {
	    writeUnsignedShort(constantPool.literalIndexForJavaLangStringBufferConstructor());
	}
	stackDepth -= 2;
    }

    public void recordPositionsFrom(int startPC, int sourcePos) {

	/* Record positions in the table, only if nothing has 
	 * already been recorded. Since we output them on the way 
	 * up (children first for more specific info)
	 * The pcToSourceMap table is always sorted.
	 */

	if (!generateLineNumberAttributes)
	    return;
	if (sourcePos == 0)
	    return;

	// no code generated for this node. e.g. field without any initialization
	if (position == startPC)
	    return;

	// Widening an existing entry that already has the same source positions
	if (pcToSourceMapSize + 4 &gt; pcToSourceMap.length) {
	    // resize the array pcToSourceMap
	    System.arraycopy(pcToSourceMap, 0, pcToSourceMap = new int[pcToSourceMapSize &lt;&lt; 1], 0, pcToSourceMapSize);
	}
	int newLine = ClassFile.searchLineNumber(lineSeparatorPositions, sourcePos);
	// lastEntryPC represents the endPC of the lastEntry.
	if (pcToSourceMapSize &gt; 0) {
	    // in this case there is already an entry in the table
	    if (pcToSourceMap[pcToSourceMapSize - 1] != newLine) {
		if (startPC &lt; lastEntryPC) {
		    // we forgot to add an entry.
		    // search if an existing entry exists for startPC
		    int insertionIndex = insertionIndex(pcToSourceMap, pcToSourceMapSize, startPC);
		    if (insertionIndex != -1) {
			// there is no existing entry starting with startPC.
			int existingEntryIndex = indexOfSameLineEntrySincePC(startPC, newLine); // index for PC
			/* the existingEntryIndex corresponds to en entry with the same line and a PC &gt;= startPC.
				in this case it is relevant to widen this entry instead of creating a new one.
				line1: this(a,
				  b,
				  c);
				with this code we generate each argument. We generate a aload0 to invoke the constructor. There is no entry for this
				aload0 bytecode. The first entry is the one for the argument a.
				But we want the constructor call to start at the aload0 pc and not just at the pc of the first argument.
				So we widen the existing entry (if there is one) or we create a new entry with the startPC.
			*/
			if (existingEntryIndex != -1) {
			    // widen existing entry
			    pcToSourceMap[existingEntryIndex] = startPC;
			} else if (insertionIndex &lt; 1 || pcToSourceMap[insertionIndex - 1] != newLine) {
			    // we have to add an entry that won't be sorted. So we sort the pcToSourceMap.
			    System.arraycopy(pcToSourceMap, insertionIndex, pcToSourceMap, insertionIndex + 2,
				    pcToSourceMapSize - insertionIndex);
			    pcToSourceMap[insertionIndex++] = startPC;
			    pcToSourceMap[insertionIndex] = newLine;
			    pcToSourceMapSize += 2;
			}
		    } else if (position != lastEntryPC) { // no bytecode since last entry pc
			pcToSourceMap[pcToSourceMapSize++] = lastEntryPC;
			pcToSourceMap[pcToSourceMapSize++] = newLine;
		    }
		} else {
		    // we can safely add the new entry. The endPC of the previous entry is not in conflit with the startPC of the new entry.
		    pcToSourceMap[pcToSourceMapSize++] = startPC;
		    pcToSourceMap[pcToSourceMapSize++] = newLine;
		}
	    } else {
		/* the last recorded entry is on the same line. But it could be relevant to widen this entry.
		   we want to extend this entry forward in case we generated some bytecode before the last entry that are not related to any statement
		*/
		if (startPC &lt; pcToSourceMap[pcToSourceMapSize - 2]) {
		    int insertionIndex = insertionIndex(pcToSourceMap, pcToSourceMapSize, startPC);
		    if (insertionIndex != -1) {
			// widen the existing entry
			// we have to figure out if we need to move the last entry at another location to keep a sorted table
			/* First we need to check if at the insertion position there is not an existing entry
			 * that includes the one we want to insert. This is the case if pcToSourceMap[insertionIndex - 1] == newLine.
			 * In this case we don't want to change the table. If not, we want to insert a new entry. Prior to insertion
			 * we want to check if it is worth doing an arraycopy. If not we simply update the recorded pc.
			 */
			if (!((insertionIndex &gt; 1) && (pcToSourceMap[insertionIndex - 1] == newLine))) {
			    if ((pcToSourceMapSize &gt; 4) && (pcToSourceMap[pcToSourceMapSize - 4] &gt; startPC)) {
				System.arraycopy(pcToSourceMap, insertionIndex, pcToSourceMap, insertionIndex + 2,
					pcToSourceMapSize - 2 - insertionIndex);
				pcToSourceMap[insertionIndex++] = startPC;
				pcToSourceMap[insertionIndex] = newLine;
			    } else {
				pcToSourceMap[pcToSourceMapSize - 2] = startPC;
			    }
			}
		    }
		}
	    }
	    lastEntryPC = position;
	} else {
	    // record the first entry
	    pcToSourceMap[pcToSourceMapSize++] = startPC;
	    pcToSourceMap[pcToSourceMapSize++] = newLine;
	    lastEntryPC = position;
	}
    }

    public void invokeStringConcatenationToString() {
	if (DEBUG) {
	    if (this.targetLevel &gt;= JDK1_5) {
		System.out.println(position + "\t\tinvokevirtual: StringBuilder.toString()Ljava.lang.String;"); //$NON-NLS-1$
	    } else {
		System.out.println(position + "\t\tinvokevirtual: StringBuffer.toString()Ljava.lang.String;"); //$NON-NLS-1$
	    }
	}
	countLabels = 0;
	if (classFileOffset + 2 &gt;= bCodeStream.length) {
	    resizeByteArray();
	}
	position++;
	bCodeStream[classFileOffset++] = OPC_invokevirtual;
	if (this.targetLevel &gt;= JDK1_5) {
	    writeUnsignedShort(constantPool.literalIndexForJavaLangStringBuilderToString());
	} else {
	    writeUnsignedShort(constantPool.literalIndexForJavaLangStringBufferToString());
	}
    }

    private final void resizeByteArray() {
	int length = bCodeStream.length;
	int requiredSize = length + length;
	if (classFileOffset &gt; requiredSize) {
	    // must be sure to grow by enough
	    requiredSize = classFileOffset + length;
	}
	System.arraycopy(bCodeStream, 0, bCodeStream = new byte[requiredSize], 0, length);
    }

    /**
    * Write a unsigned 16 bits value into the byte array
    * @param value the unsigned short
    */
    protected final void writeUnsignedShort(int value) {
	position += 2;
	bCodeStream[classFileOffset++] = (byte) (value &gt;&gt;&gt; 8);
	bCodeStream[classFileOffset++] = (byte) value;
    }

    /**
    * This methods searches for an existing entry inside the pcToSourceMap table with a pc equals to @pc.
    * If there is an existing entry it returns -1 (no insertion required).
    * Otherwise it returns the index where the entry for the pc has to be inserted.
    * This is based on the fact that the pcToSourceMap table is sorted according to the pc.
    *
    * @param pcToSourceMap the given pcToSourceMap array
    * @param length the given length
    * @param pc the given pc
    * @return int
    */
    public static int insertionIndex(int[] pcToSourceMap, int length, int pc) {
	int g = 0;
	int d = length - 2;
	int m = 0;
	while (g &lt;= d) {
	    m = (g + d) / 2;
	    // we search only on even indexes
	    if ((m % 2) != 0)
		m--;
	    int currentPC = pcToSourceMap[m];
	    if (pc &lt; currentPC) {
		d = m - 2;
	    } else if (pc &gt; currentPC) {
		g = m + 2;
	    } else {
		return -1;
	    }
	}
	if (pc &lt; pcToSourceMap[m])
	    return m;
	return m + 2;
    }

    public int indexOfSameLineEntrySincePC(int pc, int line) {
	for (int index = pc, max = pcToSourceMapSize; index &lt; max; index += 2) {
	    if (pcToSourceMap[index + 1] == line)
		return index;
	}
	return -1;
    }

}

