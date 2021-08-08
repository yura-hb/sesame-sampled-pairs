import java.io.File;
import java.text.DecimalFormat;

class Database {
    /**
     * For debugging purposes, only.
     */
    public void reportFreeBlocks() throws IndexException {
	System.out.println("Allocated size: " + formatByteString(getDatabaseSize())); //$NON-NLS-1$
	System.out.println("malloc'ed: " + formatByteString(this.malloced)); //$NON-NLS-1$
	System.out.println("free'd: " + formatByteString(this.freed)); //$NON-NLS-1$
	System.out.println("wasted: " + formatByteString((getDatabaseSize() - (this.malloced - this.freed)))); //$NON-NLS-1$
	System.out.println("Free blocks"); //$NON-NLS-1$
	for (int bs = MIN_BLOCK_DELTAS * BLOCK_SIZE_DELTA; bs &lt;= CHUNK_SIZE; bs += BLOCK_SIZE_DELTA) {
	    int count = 0;
	    long block = getFirstBlock(bs);
	    while (block != 0) {
		++count;
		block = getFreeRecPtr(block + BLOCK_NEXT_OFFSET);
	    }
	    if (count != 0)
		System.out.println("Block size: " + bs + "=" + count); //$NON-NLS-1$ //$NON-NLS-2$
	}
    }

    private long malloced;
    private long freed;
    public static final int MIN_BLOCK_DELTAS = (FREE_BLOCK_HEADER_SIZE + BLOCK_SIZE_DELTA - 1) / BLOCK_SIZE_DELTA;
    public static final int BLOCK_SIZE_DELTA = 1 &lt;&lt; BLOCK_SIZE_DELTA_BITS;
    public static final int CHUNK_SIZE = 1024 * 4;
    private static final int BLOCK_NEXT_OFFSET = BLOCK_HEADER_SIZE + INT_SIZE;
    private int fChunksUsed;
    private boolean fLocked;
    private final Chunk fHeaderChunk;
    public static final int MALLOC_TABLE_OFFSET = VERSION_OFFSET + INT_SIZE;
    public static final int INT_SIZE = 4;
    public Chunk fMostRecentlyFetchedChunk;
    private ChunkCache fCache;
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

    public long getDatabaseSize() {
	return (long) this.fChunksUsed * CHUNK_SIZE;
    }

    public static String formatByteString(long valueInBytes) {
	final double MB = 1024 * 1024;
	double value = valueInBytes;
	String suffix = "B"; //$NON-NLS-1$

	if (value &gt; 1024) {
	    suffix = "MiB"; //$NON-NLS-1$
	    value /= MB;
	}

	DecimalFormat mbFormat = new DecimalFormat("#0.###"); //$NON-NLS-1$
	return mbFormat.format(value) + suffix;
    }

    /**
     * @param blockSize (must be a multiple of BLOCK_SIZE_DELTA)
     */
    private long getFirstBlock(int blockSize) throws IndexException {
	assert this.fLocked;
	return this.fHeaderChunk.getFreeRecPtr(getAddressOfFirstBlockPointer(blockSize));
    }

    private long getFreeRecPtr(long offset) throws IndexException {
	return getChunk(offset).getFreeRecPtr(offset);
    }

    private long getAddressOfFirstBlockPointer(int blockSize) {
	return MALLOC_TABLE_OFFSET + (blockSize / BLOCK_SIZE_DELTA - MIN_BLOCK_DELTAS) * INT_SIZE;
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

