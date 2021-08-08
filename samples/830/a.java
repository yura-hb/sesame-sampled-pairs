class RandomAccessFile implements DataOutput, DataInput, Closeable {
    /**
     * Writes a {@code long} to the file as eight bytes, high byte first.
     * The write starts at the current position of the file pointer.
     *
     * @param      v   a {@code long} to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void writeLong(long v) throws IOException {
	write((int) (v &gt;&gt;&gt; 56) & 0xFF);
	write((int) (v &gt;&gt;&gt; 48) & 0xFF);
	write((int) (v &gt;&gt;&gt; 40) & 0xFF);
	write((int) (v &gt;&gt;&gt; 32) & 0xFF);
	write((int) (v &gt;&gt;&gt; 24) & 0xFF);
	write((int) (v &gt;&gt;&gt; 16) & 0xFF);
	write((int) (v &gt;&gt;&gt; 8) & 0xFF);
	write((int) (v &gt;&gt;&gt; 0) & 0xFF);
	//written += 8;
    }

    /**
     * Writes the specified byte to this file. The write starts at
     * the current file pointer.
     *
     * @param      b   the {@code byte} to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(int b) throws IOException {
	write0(b);
    }

    private native void write0(int b) throws IOException;

}

