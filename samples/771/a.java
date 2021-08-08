import org.eclipse.jdt.internal.compiler.codegen.AttributeNamesConstants;
import org.eclipse.jdt.internal.compiler.codegen.ConstantPool;

class ClassFile implements TypeConstants, TypeIds {
    /**
     * INTERNAL USE-ONLY
     * That method generates the header of a code attribute.
     * - the index inside the constant pool for the attribute name ("Code")
     * - leave some space for attribute_length(4), max_stack(2), max_locals(2), code_length(4).
     */
    public void generateCodeAttributeHeader() {
	if (this.contentsOffset + 20 &gt;= this.contents.length) {
	    resizeContents(20);
	}
	int constantValueNameIndex = this.constantPool.literalIndex(AttributeNamesConstants.CodeName);
	this.contents[this.contentsOffset++] = (byte) (constantValueNameIndex &gt;&gt; 8);
	this.contents[this.contentsOffset++] = (byte) constantValueNameIndex;
	// leave space for attribute_length(4), max_stack(2), max_locals(2), code_length(4)
	this.contentsOffset += 12;
    }

    public int contentsOffset;
    public byte[] contents;
    public ConstantPool constantPool;

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

