class ConstantPool implements ClassFileConstants, TypeIds {
    /**
     * Return the content of the receiver
     */
    public byte[] dumpBytes() {
	System.arraycopy(this.poolContent, 0, (this.poolContent = new byte[this.currentOffset]), 0, this.currentOffset);
	return this.poolContent;
    }

    public byte[] poolContent;
    public int currentOffset;

}

