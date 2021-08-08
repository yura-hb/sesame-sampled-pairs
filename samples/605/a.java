class DeferredDocumentImpl extends DocumentImpl implements DeferredNode {
    /** Ensures that the internal tables are large enough. */
    protected void ensureCapacity(int chunk) {
	if (fNodeType == null) {
	    // create buffers
	    fNodeType = new int[INITIAL_CHUNK_COUNT][];
	    fNodeName = new Object[INITIAL_CHUNK_COUNT][];
	    fNodeValue = new Object[INITIAL_CHUNK_COUNT][];
	    fNodeParent = new int[INITIAL_CHUNK_COUNT][];
	    fNodeLastChild = new int[INITIAL_CHUNK_COUNT][];
	    fNodePrevSib = new int[INITIAL_CHUNK_COUNT][];
	    fNodeURI = new Object[INITIAL_CHUNK_COUNT][];
	    fNodeExtra = new int[INITIAL_CHUNK_COUNT][];
	} else if (fNodeType.length &lt;= chunk) {
	    // resize the tables
	    int newsize = chunk * 2;

	    int[][] newArray = new int[newsize][];
	    System.arraycopy(fNodeType, 0, newArray, 0, chunk);
	    fNodeType = newArray;

	    Object[][] newStrArray = new Object[newsize][];
	    System.arraycopy(fNodeName, 0, newStrArray, 0, chunk);
	    fNodeName = newStrArray;

	    newStrArray = new Object[newsize][];
	    System.arraycopy(fNodeValue, 0, newStrArray, 0, chunk);
	    fNodeValue = newStrArray;

	    newArray = new int[newsize][];
	    System.arraycopy(fNodeParent, 0, newArray, 0, chunk);
	    fNodeParent = newArray;

	    newArray = new int[newsize][];
	    System.arraycopy(fNodeLastChild, 0, newArray, 0, chunk);
	    fNodeLastChild = newArray;

	    newArray = new int[newsize][];
	    System.arraycopy(fNodePrevSib, 0, newArray, 0, chunk);
	    fNodePrevSib = newArray;

	    newStrArray = new Object[newsize][];
	    System.arraycopy(fNodeURI, 0, newStrArray, 0, chunk);
	    fNodeURI = newStrArray;

	    newArray = new int[newsize][];
	    System.arraycopy(fNodeExtra, 0, newArray, 0, chunk);
	    fNodeExtra = newArray;
	} else if (fNodeType[chunk] != null) {
	    // Done - there's sufficient capacity
	    return;
	}

	// create new chunks
	createChunk(fNodeType, chunk);
	createChunk(fNodeName, chunk);
	createChunk(fNodeValue, chunk);
	createChunk(fNodeParent, chunk);
	createChunk(fNodeLastChild, chunk);
	createChunk(fNodePrevSib, chunk);
	createChunk(fNodeURI, chunk);
	createChunk(fNodeExtra, chunk);

	// Done
	return;

    }

    /** Node types. */
    protected transient int fNodeType[][];
    /** Initial chunk size. */
    protected static final int INITIAL_CHUNK_COUNT = (1 &lt;&lt; (13 - CHUNK_SHIFT));
    /** Node names. */
    protected transient Object fNodeName[][];
    /** Node values. */
    protected transient Object fNodeValue[][];
    /** Node parents. */
    protected transient int fNodeParent[][];
    /** Node first children. */
    protected transient int fNodeLastChild[][];
    /** Node prev siblings. */
    protected transient int fNodePrevSib[][];
    /** Node namespace URI. */
    protected transient Object fNodeURI[][];
    /** Extra data. */
    protected transient int fNodeExtra[][];
    /** Chunk size. */
    protected static final int CHUNK_SIZE = (1 &lt;&lt; CHUNK_SHIFT);
    private static final int[] INIT_ARRAY = new int[CHUNK_SIZE + 1];

    /** Creates the specified chunk in the given array of chunks. */
    private final void createChunk(int data[][], int chunk) {
	data[chunk] = new int[CHUNK_SIZE + 1];
	System.arraycopy(INIT_ARRAY, 0, data[chunk], 0, CHUNK_SIZE);
    }

    private final void createChunk(Object data[][], int chunk) {
	data[chunk] = new Object[CHUNK_SIZE + 1];
	data[chunk][CHUNK_SIZE] = new RefCount();
    }

}

