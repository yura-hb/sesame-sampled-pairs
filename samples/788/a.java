class CodeStream {
    /**
    * We didn't call it instanceof because there is a conflict with the
    * instanceof keyword
    */
    public void instance_of(TypeReference typeReference, TypeBinding typeBinding) {
	this.countLabels = 0;
	if (this.classFileOffset + 2 &gt;= this.bCodeStream.length) {
	    resizeByteArray();
	}
	this.position++;
	this.bCodeStream[this.classFileOffset++] = Opcodes.OPC_instanceof;
	writeUnsignedShort(this.constantPool.literalIndexForType(typeBinding));
    }

    public int countLabels;
    public int classFileOffset;
    public byte[] bCodeStream;
    public int position;
    public ConstantPool constantPool;

    private final void resizeByteArray() {
	int length = this.bCodeStream.length;
	int requiredSize = length + length;
	if (this.classFileOffset &gt;= requiredSize) {
	    // must be sure to grow enough
	    requiredSize = this.classFileOffset + length;
	}
	System.arraycopy(this.bCodeStream, 0, this.bCodeStream = new byte[requiredSize], 0, length);
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

}

