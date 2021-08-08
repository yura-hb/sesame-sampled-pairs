abstract class UnsafeArrayTypeWriter implements TypeWriter {
    /**
     * Copies the buffer into the provided byte[] array of length {@link #getBytesWritten()}.
     */
    public final byte[] toArray(byte[] result) {
	assert result.length == totalSize;
	int resultIdx = 0;
	for (Chunk cur = firstChunk; cur != null; cur = cur.next) {
	    System.arraycopy(cur.data, 0, result, resultIdx, cur.size);
	    resultIdx += cur.size;
	}
	assert resultIdx == totalSize;
	return result;
    }

    protected int totalSize;
    protected final Chunk firstChunk;

}

