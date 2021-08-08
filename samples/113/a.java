import java.io.RandomAccessFile;

abstract class MnistDbFile extends RandomAccessFile {
    /**
     * Move to the next entry.
     * 
     * @throws IOException
     */
    public void next() throws IOException {
	if (getCurrentIndex() &lt; count) {
	    skipBytes(getEntryLength());
	}
    }

    private int count;

    /**
     * The current entry index.
     * 
     * @return long
     * @throws IOException
     */
    public long getCurrentIndex() throws IOException {
	return (getFilePointer() - getHeaderSize()) / getEntryLength() + 1;
    }

    /**
     * Number of bytes for each entry.
     * Defaults to 1.
     * 
     * @return int
     */
    public int getEntryLength() {
	return 1;
    }

    public int getHeaderSize() {
	return 8; // two integers
    }

}

