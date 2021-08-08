abstract class Name implements Name {
    /** Return the Utf8 representation of this name.
     */
    public byte[] toUtf() {
	byte[] bs = new byte[getByteLength()];
	getBytes(bs, 0);
	return bs;
    }

    /** Get the length (in bytes) of this name.
     */
    public abstract int getByteLength();

    /** Copy all bytes of this name to buffer cs, starting at start.
     */
    public void getBytes(byte cs[], int start) {
	System.arraycopy(getByteArray(), getByteOffset(), cs, start, getByteLength());
    }

    /** Get the underlying byte array for this name. The contents of the
     * array must not be modified.
     */
    public abstract byte[] getByteArray();

    /** Get the start offset of this name within its byte array.
     */
    public abstract int getByteOffset();

}

