import java.io.File;

class Database {
    /**
     * Copies numBytes from source to destination
     */
    public void memcpy(long dest, long source, int numBytes) {
	assert numBytes &gt;= 0;
	long endAddress = source + numBytes;
	assert endAddress &lt;= (long) this.fChunksUsed * CHUNK_SIZE;
	// TODO: make use of lower-level System.arrayCopy
	for (int count = 0; count &lt; numBytes; count++) {
	    putByte(dest + count, getByte(source + count));
	}
    }

    private int fChunksUsed;
    public static final int CHUNK_SIZE = 1024 * 4;
    public Chunk fMostRecentlyFetchedChunk;
    private final Chunk fHeaderChunk;
    private ChunkCache fCache;
    private boolean fLocked;
    /**
     * Stores the {@link Chunk} associated with each page number or null if the chunk isn't loaded. Synchronize on
     * {@link #fCache} before accessing.
     */
    Chunk[] fChunks;
    private long cacheMisses;
    private long totalReadTimeMs;
    private long cacheHits;
    public static boolean DEBUG_PAGE_CACHE;
    private final File fLocation;

    public byte getByte(long offset) throws IndexException {
	return getChunk(offset).getByte(offset);
    }

    public void putByte(long offset, byte value) throws IndexException {
	getChunk(offset).putByte(offset, value);
    }

    /**
     * Return the Chunk that contains the given offset.
     * 
     * @throws IndexException
     */
    public Chunk getChunk(long offset) throws IndexException {
	assert offset &gt;= 0;
	assertLocked();
	if (offset &lt; CHUNK_SIZE) {
	    this.fMostRecentlyFetchedChunk = this.fHeaderChunk;
	    return this.fHeaderChunk;
	}
	long long_index = offset / CHUNK_SIZE;
	assert long_index &lt; Integer.MAX_VALUE;

	final int index = (int) long_index;
	Chunk chunk;
	synchronized (this.fCache) {
	    assert this.fLocked;
	    if (index &lt; 0 || index &gt;= this.fChunks.length) {
		databaseCorruptionDetected();
	    }
	    chunk = this.fChunks[index];
	}

	long readStartMs = 0;
	long readEndMs = 0;
	// Read the new chunk outside of any synchronized block (this allows parallel reads and prevents background
	// threads from retaining a lock that blocks the UI while the background thread performs I/O).
	boolean cacheMiss = (chunk == null);
	if (cacheMiss) {
	    readStartMs = System.currentTimeMillis();
	    chunk = new Chunk(this, index);
	    chunk.read();
	    readEndMs = System.currentTimeMillis();
	}

	synchronized (this.fCache) {
	    if (cacheMiss) {
		this.cacheMisses++;
		this.totalReadTimeMs += (readEndMs - readStartMs);
	    } else {
		this.cacheHits++;
	    }
	    Chunk newChunk = this.fChunks[index];
	    if (newChunk != chunk && newChunk != null) {
		// Another thread fetched this chunk in the meantime. In this case, we should use the chunk fetched
		// by the other thread.
		if (DEBUG_PAGE_CACHE) {
		    System.out.println("CHUNK " + chunk.fSequenceNumber //$NON-NLS-1$
			    + ": already fetched by another thread - instance " //$NON-NLS-1$
			    + System.identityHashCode(chunk));
		}
		chunk = newChunk;
	    } else if (cacheMiss) {
		if (DEBUG_PAGE_CACHE) {
		    System.out.println("CHUNK " + chunk.fSequenceNumber + ": inserted into vector - instance " //$NON-NLS-1$//$NON-NLS-2$
			    + System.identityHashCode(chunk));
		}
		this.fChunks[index] = chunk;
	    }
	    this.fCache.add(chunk);
	    this.fMostRecentlyFetchedChunk = chunk;
	}

	return chunk;
    }

    public void assertLocked() {
	if (!this.fLocked) {
	    throw new IllegalStateException("Database not locked!"); //$NON-NLS-1$
	}
    }

    private void databaseCorruptionDetected() throws IndexException {
	String msg = "Corrupted database: " + this.fLocation.getName(); //$NON-NLS-1$
	throw new IndexException(new DBStatus(msg));
    }

}

