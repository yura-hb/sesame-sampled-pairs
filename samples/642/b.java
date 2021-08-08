import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jdt.internal.core.nd.IndexExceptionBuilder;

class Database {
    /**
     * Allocate a block out of the database.
     */
    public long malloc(final long datasize, final short poolId) throws IndexException {
	assert this.fExclusiveLock;
	assert datasize &gt;= 0;
	assert datasize &lt;= MAX_MALLOC_SIZE;

	long result;
	int usedSize;
	this.log.start(this.mallocTag);
	try {
	    if (datasize &gt;= MAX_SINGLE_BLOCK_MALLOC_SIZE) {
		int newChunkNum = createLargeBlock(datasize);
		usedSize = Math.abs(getBlockHeaderForChunkNum(newChunkNum)) * CHUNK_SIZE;
		result = (long) newChunkNum * CHUNK_SIZE + LargeBlock.HEADER_SIZE;
		// Note that we identify large blocks by setting their block size to 0.
		clearRange(result, usedSize - LargeBlock.HEADER_SIZE - LargeBlock.FOOTER_SIZE);
		result = result + BLOCK_HEADER_SIZE;
	    } else {
		long freeBlock = 0;
		int needDeltas = divideRoundingUp(datasize + BLOCK_HEADER_SIZE, BLOCK_SIZE_DELTA);
		if (needDeltas &lt; MIN_BLOCK_DELTAS) {
		    needDeltas = MIN_BLOCK_DELTAS;
		}

		// Which block size.
		int useDeltas;
		for (useDeltas = needDeltas; useDeltas &lt;= MAX_BLOCK_DELTAS; useDeltas++) {
		    freeBlock = getFirstBlock(useDeltas * BLOCK_SIZE_DELTA);
		    if (freeBlock != 0)
			break;
		}

		// Get the block.
		Chunk chunk;
		if (freeBlock == 0) {
		    // Allocate a new chunk.
		    freeBlock = (long) (createLargeBlock(datasize)) * (long) CHUNK_SIZE + LargeBlock.HEADER_SIZE;
		    useDeltas = MAX_BLOCK_DELTAS;
		    chunk = getChunk(freeBlock);
		} else {
		    chunk = getChunk(freeBlock);
		    chunk.makeDirty();
		    int blockReportedSize = chunk.getShort(freeBlock);
		    if (blockReportedSize != useDeltas * BLOCK_SIZE_DELTA) {
			throw describeProblem().addProblemAddress("block size", freeBlock, SHORT_SIZE) //$NON-NLS-1$
				.build("Heap corruption detected in free space list. Block " + freeBlock //$NON-NLS-1$
					+ " reports a size of " + blockReportedSize //$NON-NLS-1$
					+ " but was in the list for blocks of size " //$NON-NLS-1$
					+ useDeltas * BLOCK_SIZE_DELTA);
		    }
		    removeBlock(chunk, useDeltas * BLOCK_SIZE_DELTA, freeBlock);
		}

		final int unusedDeltas = useDeltas - needDeltas;
		if (unusedDeltas &gt;= MIN_BLOCK_DELTAS) {
		    // Add in the unused part of our block.
		    addBlock(chunk, unusedDeltas * BLOCK_SIZE_DELTA, freeBlock + needDeltas * BLOCK_SIZE_DELTA);
		    useDeltas = needDeltas;
		}

		// Make our size negative to show in use.
		usedSize = useDeltas * BLOCK_SIZE_DELTA;
		chunk.putShort(freeBlock, (short) -usedSize);

		// Clear out the block, lots of people are expecting this.
		chunk.clear(freeBlock + BLOCK_HEADER_SIZE, usedSize - BLOCK_HEADER_SIZE);
		result = freeBlock + BLOCK_HEADER_SIZE;
	    }
	} finally {
	    this.log.end(this.mallocTag);
	}

	this.log.recordMalloc(result, usedSize - BLOCK_HEADER_SIZE);
	this.malloced += usedSize;
	this.memoryUsage.recordMalloc(poolId, usedSize);

	if (DEBUG_FREE_SPACE) {
	    boolean performedValidation = periodicValidateFreeSpace();

	    if (performedValidation) {
		verifyNotInFreeSpaceList(result);
	    }
	}

	return result;
    }

    private boolean fExclusiveLock;
    public static final long MAX_MALLOC_SIZE = MAX_DB_SIZE - LargeBlock.HEADER_SIZE - LargeBlock.FOOTER_SIZE
	    - CHUNK_SIZE - BLOCK_HEADER_SIZE;
    private final ModificationLog log = new ModificationLog(0);
    private final Tag mallocTag;
    public static final int MAX_SINGLE_BLOCK_MALLOC_SIZE = MAX_BLOCK_DELTAS * BLOCK_SIZE_DELTA - BLOCK_HEADER_SIZE;
    public static final int CHUNK_SIZE = 1024 * 4;
    public static final int BLOCK_HEADER_SIZE = SHORT_SIZE;
    public static final int BLOCK_SIZE_DELTA = 1 &lt;&lt; BLOCK_SIZE_DELTA_BITS;
    public static final int MIN_BLOCK_DELTAS = (FREE_BLOCK_HEADER_SIZE + BLOCK_SIZE_DELTA - 1) / BLOCK_SIZE_DELTA;
    public static final int MAX_BLOCK_DELTAS = (CHUNK_SIZE - LargeBlock.HEADER_SIZE - LargeBlock.FOOTER_SIZE)
	    / BLOCK_SIZE_DELTA;
    public static final int SHORT_SIZE = 2;
    private long malloced;
    private MemoryStats memoryUsage;
    /**
     * True iff large chunk self-diagnostics should be enabled.
     */
    public static boolean DEBUG_FREE_SPACE;
    private int fChunksUsed;
    public static final int INT_SIZE = 4;
    private boolean fLocked;
    private final Chunk fHeaderChunk;
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
    private static final int BLOCK_PREV_OFFSET = BLOCK_HEADER_SIZE;
    private static final int BLOCK_NEXT_OFFSET = BLOCK_HEADER_SIZE + INT_SIZE;
    private long validateCounter;
    private long nextValidation;
    public static final int PTR_SIZE = 4;
    public static final int FREE_BLOCK_OFFSET = MALLOC_TABLE_OFFSET
	    + (CHUNK_SIZE / BLOCK_SIZE_DELTA - MIN_BLOCK_DELTAS + 1) * INT_SIZE;
    public static final long MAX_DB_SIZE = ((long) 1 &lt;&lt; (Integer.SIZE + BLOCK_SIZE_DELTA_BITS));
    public static final int MALLOC_TABLE_OFFSET = VERSION_OFFSET + INT_SIZE;
    private final File fLocation;

    /**
     * Obtains a new block that can fit the given number of bytes (at minimum). Returns the
     * chunk number.
     * 
     * @param datasize minimum number of bytes needed
     * @return the chunk number
     */
    private int createLargeBlock(long datasize) {
	final int neededChunks = getChunksNeededForBytes(datasize);
	int freeBlockChunkNum = getFreeBlockFromTrie(neededChunks);
	final int numChunks;

	if (freeBlockChunkNum == 0) {
	    final int lastChunkNum = this.fChunksUsed;

	    numChunks = neededChunks;

	    // Check if the last block in the database is free. If so, unlink and expand it.
	    int lastBlockSize = getBlockFooterForChunkBefore(lastChunkNum);
	    if (lastBlockSize &gt; 0) {
		int startChunkNum = getFirstChunkOfBlockBefore(lastChunkNum);

		unlinkFreeBlock(startChunkNum);
		// Allocate additional new chunks such that the new chunk is large enough to
		// handle this allocation.
		createNewChunks(neededChunks - lastBlockSize);
		freeBlockChunkNum = startChunkNum;
	    } else {
		freeBlockChunkNum = createNewChunks(numChunks);
	    }
	} else {
	    numChunks = getBlockHeaderForChunkNum(freeBlockChunkNum);

	    if (numChunks &lt; neededChunks) {
		throw describeProblem()
			.addProblemAddress("chunk header", (long) freeBlockChunkNum * CHUNK_SIZE, INT_SIZE) //$NON-NLS-1$
			.build("A block in the free space trie was too small or wasn't actually free. Reported size = " //$NON-NLS-1$
				+ numChunks + " chunks, requested size = " + neededChunks + " chunks"); //$NON-NLS-1$//$NON-NLS-2$
	    }

	    int footer = getBlockFooterForChunkBefore(freeBlockChunkNum + numChunks);
	    if (footer != numChunks) {
		throw describeProblem()
			.addProblemAddress("chunk header", (long) freeBlockChunkNum * CHUNK_SIZE, INT_SIZE) //$NON-NLS-1$
			.addProblemAddress("chunk footer", //$NON-NLS-1$
				(long) (freeBlockChunkNum + numChunks) * CHUNK_SIZE - INT_SIZE, INT_SIZE).build("The header and footer didn't match for a block in the free space trie. Expected " //$NON-NLS-1$
				+ numChunks + " but found " + footer); //$NON-NLS-1$
	    }

	    unlinkFreeBlock(freeBlockChunkNum);
	}

	final int resultChunkNum;
	if (numChunks &gt; neededChunks) {
	    // If the chunk we've selected is larger than necessary, split it. We have the
	    // choice of using either half of the block. In the interest of leaving more
	    // opportunities of merging large blocks, we leave the unused half of the block
	    // next to the larger adjacent block.
	    final int nextBlockChunkNum = freeBlockChunkNum + numChunks;

	    final int nextBlockSize = Math.abs(getBlockHeaderForChunkNum(nextBlockChunkNum));
	    final int prevBlockSize = Math.abs(getBlockFooterForChunkBefore(freeBlockChunkNum));

	    final int unusedChunks = numChunks - neededChunks;
	    if (nextBlockSize &gt;= prevBlockSize) {
		// Use the start of the block
		resultChunkNum = freeBlockChunkNum;
		// Return the last half of the block to the free block pool
		linkFreeBlockToTrie(freeBlockChunkNum + neededChunks, unusedChunks);
	    } else {
		// Use the end of the block
		resultChunkNum = freeBlockChunkNum + unusedChunks;
		// Return the first half of the block to the free block pool
		linkFreeBlockToTrie(freeBlockChunkNum, unusedChunks);
	    }
	} else {
	    resultChunkNum = freeBlockChunkNum;
	}

	// Fill in the header and footer
	setBlockHeader(resultChunkNum, -neededChunks);
	return resultChunkNum;
    }

    /**
     * Returns the size of the block (in number of chunks) starting at the given address. The return value is positive
     * if the block is free and negative if the block is allocated.
     */
    private int getBlockHeaderForChunkNum(int firstChunkNum) {
	if (firstChunkNum &gt;= this.fChunksUsed) {
	    return 0;
	}
	return getInt((long) firstChunkNum * CHUNK_SIZE);
    }

    /**
     * Clears all the bytes in the given range by setting them to zero.
     * 
     * @param startAddress first address to clear
     * @param bytesToClear number of addresses to clear
     */
    public void clearRange(long startAddress, long bytesToClear) {
	if (bytesToClear == 0) {
	    return;
	}
	long endAddress = startAddress + bytesToClear;
	assert endAddress &lt;= (long) this.fChunksUsed * CHUNK_SIZE;
	int blockNumber = (int) (startAddress / CHUNK_SIZE);
	int firstBlockBytesToClear = (int) Math.min((((long) (blockNumber + 1) * CHUNK_SIZE) - startAddress),
		bytesToClear);

	Chunk firstBlock = getChunk(startAddress);
	firstBlock.clear(startAddress, firstBlockBytesToClear);
	startAddress += firstBlockBytesToClear;
	bytesToClear -= firstBlockBytesToClear;
	while (bytesToClear &gt; CHUNK_SIZE) {
	    Chunk nextBlock = getChunk(startAddress);
	    nextBlock.clear(startAddress, CHUNK_SIZE);
	    startAddress += CHUNK_SIZE;
	    bytesToClear -= CHUNK_SIZE;
	}

	if (bytesToClear &gt; 0) {
	    Chunk nextBlock = getChunk(startAddress);
	    nextBlock.clear(startAddress, (int) bytesToClear);
	}
    }

    private static int divideRoundingUp(long num, long den) {
	return (int) ((num + den - 1) / den);
    }

    /**
     * @param blockSize (must be a multiple of BLOCK_SIZE_DELTA)
     */
    private long getFirstBlock(int blockSize) throws IndexException {
	assert this.fLocked;
	return this.fHeaderChunk.getFreeRecPtr(getAddressOfFirstBlockPointer(blockSize));
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

    public IndexExceptionBuilder describeProblem() {
	return new IndexExceptionBuilder(this);
    }

    private void removeBlock(Chunk chunk, int blocksize, long block) throws IndexException {
	assert this.fExclusiveLock;

	long prevblock = chunk.getFreeRecPtr(block + BLOCK_PREV_OFFSET);
	long nextblock = chunk.getFreeRecPtr(block + BLOCK_NEXT_OFFSET);
	if (prevblock != 0) {
	    putFreeRecPtr(prevblock + BLOCK_NEXT_OFFSET, nextblock);
	} else { // We were the head.
	    setFirstBlock(blocksize, nextblock);
	}

	if (nextblock != 0)
	    putFreeRecPtr(nextblock + BLOCK_PREV_OFFSET, prevblock);
    }

    private void addBlock(Chunk chunk, int blocksize, long block) throws IndexException {
	assert this.fExclusiveLock;
	// Mark our size
	chunk.putShort(block, (short) blocksize);

	// Add us to the head of the list.
	long prevfirst = getFirstBlock(blocksize);
	chunk.putFreeRecPtr(block + BLOCK_PREV_OFFSET, 0);
	chunk.putFreeRecPtr(block + BLOCK_NEXT_OFFSET, prevfirst);
	if (prevfirst != 0)
	    putFreeRecPtr(prevfirst + BLOCK_PREV_OFFSET, block);
	setFirstBlock(blocksize, block);
    }

    /**
     * Periodically performs validation of the free space in the database. Validation is very expensive, so the
     * validation period uses exponential falloff so validations happen less and less frequently over
     * time. Returns true iff validation happened on this iteration.
     */
    private boolean periodicValidateFreeSpace() {
	this.validateCounter++;
	if (this.validateCounter &gt; this.nextValidation) {
	    validateFreeSpace();
	    this.nextValidation = this.validateCounter * 2;
	    return true;
	}
	return false;
    }

    private void verifyNotInFreeSpaceList(long result) {
	int useDeltas;
	for (useDeltas = MIN_BLOCK_DELTAS; useDeltas &lt;= MAX_BLOCK_DELTAS; useDeltas++) {
	    int correctSize = useDeltas * BLOCK_SIZE_DELTA;
	    long block = getFirstBlock(correctSize);
	    long addressOfPrevBlockPointer = getAddressOfFirstBlockPointer(correctSize);
	    while (block != 0) {
		if (block == result) {
		    throw describeProblem().addProblemAddress("incoming pointer", addressOfPrevBlockPointer, PTR_SIZE) //$NON-NLS-1$
			    .build("Block " + result //$NON-NLS-1$
				    + " was found in the free space list, even though it wasn't free"); //$NON-NLS-1$
		}
		addressOfPrevBlockPointer = block + BLOCK_NEXT_OFFSET;
		long followingBlock = getFreeRecPtr(addressOfPrevBlockPointer);
		block = followingBlock;
	    }
	}

	int currentChunkNum = getInt(FREE_BLOCK_OFFSET);

	if (currentChunkNum == 0) {
	    return;
	}
	int targetChunkNum = (int) (result / CHUNK_SIZE);

	if (currentChunkNum == targetChunkNum) {
	    throw describeProblem().build("Block " + result //$NON-NLS-1$
		    + " was not supposed to be in the free space list, but was linked as the root of the list"); //$NON-NLS-1$
	}

	verifyNotInLargeBlockFreeSpaceTrie(targetChunkNum, currentChunkNum, 0);
    }

    /**
     * Returns the number of chunks needed to fit the given number of bytes of payload.
     */
    public static int getChunksNeededForBytes(long datasize) {
	return divideRoundingUp(datasize + BLOCK_HEADER_SIZE + LargeBlock.HEADER_SIZE + LargeBlock.FOOTER_SIZE,
		CHUNK_SIZE);
    }

    /**
     * Returns the chunk number of a free block that contains at least the given number of chunks, or
     * 0 if there is no existing contiguous free block containing at least the given number of chunks.
     * 
     * @param numChunks minumum number of chunks desired
     * @return the chunk number of a free block containing at least the given number of chunks or 0
     * if there is no existing free block containing that many chunks.
     */
    private int getFreeBlockFromTrie(int numChunks) {
	int currentChunkNum = getInt(FREE_BLOCK_OFFSET);

	int resultChunkNum = getSmallestChildNoSmallerThan(currentChunkNum, numChunks);
	if (resultChunkNum == 0) {
	    return 0;
	}

	// Try not to return the trie node itself if there is a linked list entry available, since unlinking
	// something from the linked list is faster than unlinking a trie node.
	int nextResultChunkNum = getInt((long) resultChunkNum * CHUNK_SIZE + LargeBlock.NEXT_BLOCK_OFFSET);
	if (nextResultChunkNum != 0) {
	    return nextResultChunkNum;
	}
	return resultChunkNum;
    }

    /**
     * Returns the size of the block (in number of chunks), given the (non-inclusive) address that the block ends at.
     * The return value is positive if the block is free and negative if the block is allocated.
     */
    private int getBlockFooterForChunkBefore(int chunkNum) {
	if (chunkNum &lt; 2) {
	    // Don't report the database header as a normal chunk.
	    return 0;
	}
	return getInt((long) chunkNum * CHUNK_SIZE - LargeBlock.FOOTER_SIZE);
    }

    /**
     * Returns the chunk number of the chunk at the start of a block, given the
     * chunk number of the chunk at the start of the following block.
     * 
     * @param chunkNum the chunk number of the chunk immediately following the
     * chunk being queried
     * @return the chunk number of the chunk at the start of the previous block
     */
    private int getFirstChunkOfBlockBefore(int chunkNum) {
	int blockChunks = Math.abs(getBlockFooterForChunkBefore(chunkNum));
	return chunkNum - blockChunks;
    }

    /**
     * Unlinks a free block (which currently belongs to the free block trie) so that it may
     * be reused.
     * 
     * @param freeBlockChunkNum chunk number of the block to be unlinked
     */
    private void unlinkFreeBlock(int freeBlockChunkNum) {
	long freeBlockAddress = (long) freeBlockChunkNum * CHUNK_SIZE;
	int anotherBlockOfSameSize = 0;
	int nextBlockChunkNum = getInt(freeBlockAddress + LargeBlock.NEXT_BLOCK_OFFSET);
	int prevBlockChunkNum = getInt(freeBlockAddress + LargeBlock.PREV_BLOCK_OFFSET);
	// Relink the linked list
	if (nextBlockChunkNum != 0) {
	    anotherBlockOfSameSize = nextBlockChunkNum;
	    putInt((long) nextBlockChunkNum * CHUNK_SIZE + LargeBlock.PREV_BLOCK_OFFSET, prevBlockChunkNum);
	}
	if (prevBlockChunkNum != 0) {
	    anotherBlockOfSameSize = prevBlockChunkNum;
	    putInt((long) prevBlockChunkNum * CHUNK_SIZE + LargeBlock.NEXT_BLOCK_OFFSET, nextBlockChunkNum);
	}

	/**
	 * True iff this block was a block in the trie. False if it was attached to to the list of siblings but some
	 * other node in the list is the one in the trie.
	 */
	boolean wasInTrie = false;
	long root = getInt(FREE_BLOCK_OFFSET);
	if (root == freeBlockChunkNum) {
	    putInt(FREE_BLOCK_OFFSET, 0);
	    wasInTrie = true;
	}

	int freeBlockSize = getBlockHeaderForChunkNum(freeBlockChunkNum);
	int parentChunkNum = getInt(freeBlockAddress + LargeBlock.PARENT_OFFSET);
	if (parentChunkNum != 0) {
	    int currentSize = getBlockHeaderForChunkNum(parentChunkNum);
	    int difference = currentSize ^ freeBlockSize;
	    if (difference != 0) {
		int firstDifference = LargeBlock.SIZE_OF_SIZE_FIELD * 8 - Integer.numberOfLeadingZeros(difference) - 1;
		long locationOfChildPointer = (long) parentChunkNum * CHUNK_SIZE + LargeBlock.CHILD_TABLE_OFFSET
			+ (firstDifference * INT_SIZE);
		int childChunkNum = getInt(locationOfChildPointer);
		if (childChunkNum == freeBlockChunkNum) {
		    wasInTrie = true;
		    putInt(locationOfChildPointer, 0);
		}
	    }
	}

	// If the removed block was the head of the linked list, we need to reinsert the following entry as the
	// new head.
	if (wasInTrie && anotherBlockOfSameSize != 0) {
	    insertChild(parentChunkNum, anotherBlockOfSameSize);
	}

	int currentParent = parentChunkNum;
	for (int childIdx = 0; childIdx &lt; LargeBlock.ENTRIES_IN_CHILD_TABLE; childIdx++) {
	    long childAddress = freeBlockAddress + LargeBlock.CHILD_TABLE_OFFSET + (childIdx * INT_SIZE);
	    int nextChildChunkNum = getInt(childAddress);
	    if (nextChildChunkNum != 0) {
		if (!wasInTrie) {
		    throw describeProblem().addProblemAddress("non-null child pointer", childAddress, INT_SIZE) //$NON-NLS-1$
			    .build("All child pointers should be null for a free chunk that is in the sibling list but" //$NON-NLS-1$
				    + " not part of the trie. Problematic chunk number: " + freeBlockChunkNum); //$NON-NLS-1$
		}
		insertChild(currentParent, nextChildChunkNum);
		// Parent all subsequent children under the child that was most similar to the old parent
		if (currentParent == parentChunkNum) {
		    currentParent = nextChildChunkNum;
		}
	    }
	}

    }

    private int createNewChunks(int numChunks) throws IndexException {
	assert this.fExclusiveLock;
	synchronized (this.fCache) {
	    final int firstChunkIndex = this.fChunksUsed;
	    final int lastChunkIndex = firstChunkIndex + numChunks - 1;

	    final Chunk lastChunk = new Chunk(this, lastChunkIndex);

	    if (lastChunkIndex &gt;= this.fChunks.length) {
		int increment = Math.max(1024, this.fChunks.length / 20);
		int newNumChunks = Math.max(lastChunkIndex + 1, this.fChunks.length + increment);
		Chunk[] newChunks = new Chunk[newNumChunks];
		System.arraycopy(this.fChunks, 0, newChunks, 0, this.fChunks.length);
		this.fChunks = newChunks;
	    }

	    this.fChunksUsed = lastChunkIndex + 1;
	    if (DEBUG_PAGE_CACHE) {
		System.out.println("CHUNK " + lastChunk.fSequenceNumber + ": inserted into vector - instance " //$NON-NLS-1$//$NON-NLS-2$
			+ System.identityHashCode(lastChunk));
	    }
	    this.fChunks[lastChunkIndex] = lastChunk;
	    this.fMostRecentlyFetchedChunk = lastChunk;
	    lastChunk.makeDirty();
	    this.fCache.add(lastChunk);
	    long result = (long) firstChunkIndex * CHUNK_SIZE;

	    /*
	     * Non-dense pointers are at most 31 bits dense pointers are at most 35 bits Check the sizes here and throw
	     * an exception if the address is too large. By throwing the IndexException with the special status, the
	     * indexing operation should be stopped. This is desired since generally, once the max size is exceeded,
	     * there are lots of errors.
	     */
	    long endAddress = result + ((long) numChunks * CHUNK_SIZE);
	    if (endAddress &gt; MAX_DB_SIZE) {
		Object bindings[] = { this.getLocation().getAbsolutePath(), MAX_DB_SIZE };
		throw new IndexException(new Status(IStatus.ERROR, Package.PLUGIN_ID, Package.STATUS_DATABASE_TOO_LARGE,
			NLS.bind("Database too large! Address = " + endAddress + ", max size = " + MAX_DB_SIZE, //$NON-NLS-1$ //$NON-NLS-2$
				bindings),
			null));
	    }

	    return firstChunkIndex;
	}
    }

    /**
     * Link the given unused block into the free block tries. The block does not need to have
     * its header filled in already.
     * 
     * @param freeBlockChunkNum chunk number of the start of the block
     * @param numChunks number of chunks in the block
     */
    private void linkFreeBlockToTrie(int freeBlockChunkNum, int numChunks) {
	setBlockHeader(freeBlockChunkNum, numChunks);
	long freeBlockAddress = (long) freeBlockChunkNum * CHUNK_SIZE;
	Chunk chunk = getChunk(freeBlockAddress);
	chunk.clear(freeBlockAddress + LargeBlock.HEADER_SIZE,
		LargeBlock.UNALLOCATED_HEADER_SIZE - LargeBlock.HEADER_SIZE);

	insertChild(getInt(FREE_BLOCK_OFFSET), freeBlockChunkNum);
    }

    /**
     * Sets the block header and footer for the given range of chunks which make
     * up a contiguous block.
     * 
     * @param firstChunkNum chunk number of the first chunk in the block
     * @param headerContent the content of the header. Its magnitude is the number of
     * chunks in the block. It is positive if the chunk is free and negative if
     * the chunk is in use.
     */
    private void setBlockHeader(int firstChunkNum, int headerContent) {
	assert headerContent != 0;
	assert firstChunkNum &lt; this.fChunksUsed;
	int numBlocks = Math.abs(headerContent);
	long firstChunkAddress = (long) firstChunkNum * CHUNK_SIZE;
	putInt(firstChunkAddress, headerContent);
	putInt(firstChunkAddress + ((long) numBlocks * CHUNK_SIZE) - LargeBlock.FOOTER_SIZE, headerContent);
    }

    public int getInt(long offset) throws IndexException {
	return getChunk(offset).getInt(offset);
    }

    private long getAddressOfFirstBlockPointer(int blockSize) {
	return MALLOC_TABLE_OFFSET + (blockSize / BLOCK_SIZE_DELTA - MIN_BLOCK_DELTAS) * INT_SIZE;
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

    private void putFreeRecPtr(long offset, long value) throws IndexException {
	getChunk(offset).putFreeRecPtr(offset, value);
    }

    private void setFirstBlock(int blockSize, long block) throws IndexException {
	assert this.fExclusiveLock;
	this.fHeaderChunk.putFreeRecPtr(getAddressOfFirstBlockPointer(blockSize), block);
    }

    public void validateFreeSpace() {
	validateFreeSpaceLists();
	validateFreeSpaceTries();
    }

    private long getFreeRecPtr(long offset) throws IndexException {
	return getChunk(offset).getFreeRecPtr(offset);
    }

    private void verifyNotInLargeBlockFreeSpaceTrie(int targetChunkNum, int chunkNum, int parent) {
	long chunkStart = (long) chunkNum * CHUNK_SIZE;

	for (int testPosition = 0; testPosition &lt; LargeBlock.ENTRIES_IN_CHILD_TABLE; testPosition++) {
	    long chunkAddress = chunkStart + LargeBlock.CHILD_TABLE_OFFSET + (testPosition * INT_SIZE);
	    int nextChildChunkNum = getInt(chunkAddress);

	    if (nextChildChunkNum == 0) {
		continue;
	    }

	    if (nextChildChunkNum == targetChunkNum) {
		throw describeProblem().addProblemAddress("trie child address", chunkAddress, INT_SIZE) //$NON-NLS-1$
			.build("Chunk number " + nextChildChunkNum //$NON-NLS-1$
				+ " was found in the free space trie even though it was in use"); //$NON-NLS-1$
	    }

	    verifyNotInLargeBlockFreeSpaceTrie(targetChunkNum, nextChildChunkNum, chunkNum);
	}
    }

    /**
     * Given the chunk number of a block somewhere in the free space trie, this returns the smallest
     * child in the subtree that is no smaller than the given number of chunks.
     * 
     * @param trieNodeChunkNum chunk number of a block in the free space trie
     * @param numChunks desired number of chunks
     * @return the chunk number of the first chunk in a contiguous free block containing at least the
     * given number of chunks
     */
    private int getSmallestChildNoSmallerThan(int trieNodeChunkNum, int numChunks) {
	if (trieNodeChunkNum == 0) {
	    return 0;
	}
	int currentSize = getBlockHeaderForChunkNum(trieNodeChunkNum);
	assert (currentSize &gt;= 0);
	int difference = currentSize ^ numChunks;
	if (difference == 0) {
	    return trieNodeChunkNum;
	}

	int bitMask = Integer.highestOneBit(difference);
	int firstDifference = LargeBlock.SIZE_OF_SIZE_FIELD * 8 - Integer.numberOfLeadingZeros(bitMask) - 1;
	boolean lookingForSmallerChild = (currentSize &gt; numChunks);
	for (int testPosition = firstDifference; testPosition &lt; LargeBlock.ENTRIES_IN_CHILD_TABLE; testPosition++) {
	    if (((currentSize & bitMask) != 0) == lookingForSmallerChild) {
		int nextChildChunkNum = getInt((long) trieNodeChunkNum * CHUNK_SIZE + LargeBlock.CHILD_TABLE_OFFSET
			+ (testPosition * INT_SIZE));
		int childResultChunkNum = getSmallestChildNoSmallerThan(nextChildChunkNum, numChunks);
		if (childResultChunkNum != 0) {
		    return childResultChunkNum;
		}
	    }
	    bitMask &lt;&lt;= 1;
	}

	if (lookingForSmallerChild) {
	    return trieNodeChunkNum;
	} else {
	    return 0;
	}
    }

    public void putInt(long offset, int value) throws IndexException {
	getChunk(offset).putInt(offset, value);
    }

    /**
     * Adds the given child block to the given parent subtree of the free space trie. Any existing
     * subtree under the given child block will be retained.
     * 
     * @param parentChunkNum root of the existing tree, or 0 if the child is going to be the new root
     * @param newChildChunkNum the new child to insert
     */
    private void insertChild(int parentChunkNum, int newChildChunkNum) {
	if (parentChunkNum == 0) {
	    putInt((long) newChildChunkNum * CHUNK_SIZE + LargeBlock.PARENT_OFFSET, parentChunkNum);
	    putInt(FREE_BLOCK_OFFSET, newChildChunkNum);
	    return;
	}
	int numChunks = getBlockHeaderForChunkNum(newChildChunkNum);
	for (;;) {
	    int currentSize = getBlockHeaderForChunkNum(parentChunkNum);
	    int difference = currentSize ^ numChunks;
	    if (difference == 0) {
		// The newly added item is exactly the same size as this trie node
		insertFreeBlockAfter(parentChunkNum, newChildChunkNum);
		return;
	    }

	    int firstDifference = LargeBlock.SIZE_OF_SIZE_FIELD * 8 - Integer.numberOfLeadingZeros(difference) - 1;
	    long locationOfChildPointer = (long) parentChunkNum * CHUNK_SIZE + LargeBlock.CHILD_TABLE_OFFSET
		    + (firstDifference * INT_SIZE);
	    int childChunkNum = getInt(locationOfChildPointer);
	    if (childChunkNum == 0) {
		putInt(locationOfChildPointer, newChildChunkNum);
		putInt((long) newChildChunkNum * CHUNK_SIZE + LargeBlock.PARENT_OFFSET, parentChunkNum);
		return;
	    }
	    parentChunkNum = childChunkNum;
	}
    }

    /**
     * This method is public for testing purposes only.
     */
    public File getLocation() {
	return this.fLocation;
    }

    /**
     * Performs a self-test on the free space lists used by malloc to check for corruption
     */
    private void validateFreeSpaceLists() {
	int useDeltas;
	for (useDeltas = MIN_BLOCK_DELTAS; useDeltas &lt;= MAX_BLOCK_DELTAS; useDeltas++) {
	    validateFreeBlocksFor(useDeltas);
	}
    }

    /**
     * Performs a self-test on the free space trie list (used by the large block allocator) to check for corruption
     */
    private void validateFreeSpaceTries() {
	int currentChunkNum = getInt(FREE_BLOCK_OFFSET);

	if (currentChunkNum == 0) {
	    return;
	}

	Set&lt;Integer&gt; visited = new HashSet&lt;&gt;();
	validateFreeSpaceNode(visited, currentChunkNum, 0);
    }

    /**
     * Adds the given block to the linked list of equally-sized free chunks in the free space trie.
     * Both chunks must be unused, must be the same size, and the previous chunk must already
     * be linked into the free space trie. The newly-added chunk must not have any children.
     * 
     * @param prevChunkNum chunk number of previous block in the existing list
     * @param newChunkNum new chunk to be added to the list
     */
    private void insertFreeBlockAfter(int prevChunkNum, int newChunkNum) {
	long prevChunkAddress = (long) prevChunkNum * CHUNK_SIZE;
	int nextChunkNum = getInt(prevChunkAddress + LargeBlock.NEXT_BLOCK_OFFSET);
	long nextChunkAddress = (long) nextChunkNum * CHUNK_SIZE;
	long newLockAddress = (long) newChunkNum * CHUNK_SIZE;

	putInt(prevChunkAddress + LargeBlock.NEXT_BLOCK_OFFSET, newChunkNum);
	if (nextChunkNum != 0) {
	    putInt(nextChunkAddress + LargeBlock.PREV_BLOCK_OFFSET, newChunkNum);
	}
	putInt(newLockAddress + LargeBlock.PREV_BLOCK_OFFSET, prevChunkNum);
	putInt(newLockAddress + LargeBlock.NEXT_BLOCK_OFFSET, nextChunkNum);
    }

    private void validateFreeBlocksFor(int numberOfDeltas) {
	int correctSize = numberOfDeltas * BLOCK_SIZE_DELTA;
	long lastBlock = 0;
	long block = getFirstBlock(correctSize);
	long addressOfPrevBlockPointer = getAddressOfFirstBlockPointer(correctSize);
	while (block != 0) {
	    long measuredLastBlock = getFreeRecPtr(block + BLOCK_PREV_OFFSET);
	    int blockReportedSize = getShort(block);
	    long followingBlock = getFreeRecPtr(block + BLOCK_NEXT_OFFSET);
	    if (measuredLastBlock != lastBlock) {
		throw describeProblem().addProblemAddress("last block", block + BLOCK_PREV_OFFSET, PTR_SIZE) //$NON-NLS-1$
			.addProblemAddress("incoming pointer", addressOfPrevBlockPointer, PTR_SIZE) //$NON-NLS-1$
			.build("The free space block (" + block //$NON-NLS-1$
				+ ") of size " + correctSize + " had an incorrect prev pointer to " //$NON-NLS-1$//$NON-NLS-2$
				+ measuredLastBlock + ", but it should have been pointing to " //$NON-NLS-1$
				+ lastBlock);
	    }
	    if (blockReportedSize != correctSize) {
		throw describeProblem().addProblemAddress("block size", block, SHORT_SIZE) //$NON-NLS-1$
			.addProblemAddress("incoming pointer", addressOfPrevBlockPointer, PTR_SIZE) //$NON-NLS-1$
			.build("A block (" + block + ") of size " + measuredLastBlock //$NON-NLS-1$ //$NON-NLS-2$
				+ " was in the free space list for blocks of size " + correctSize); //$NON-NLS-1$
	    }
	    addressOfPrevBlockPointer = block + BLOCK_NEXT_OFFSET;
	    lastBlock = block;
	    block = followingBlock;
	}
    }

    private void validateFreeSpaceNode(Set&lt;Integer&gt; visited, int chunkNum, int parent) {
	if (visited.contains(chunkNum)) {
	    throw describeProblem().build("Chunk " + chunkNum + "(parent = " + parent //$NON-NLS-1$//$NON-NLS-2$
		    + " appeared twice in the free space tree"); //$NON-NLS-1$
	}

	long chunkStart = (long) chunkNum * CHUNK_SIZE;
	int parentChunk = getInt(chunkStart + LargeBlock.PARENT_OFFSET);
	if (parentChunk != parent) {
	    throw describeProblem()
		    .addProblemAddress("parent pointer", chunkStart + LargeBlock.PARENT_OFFSET, Database.INT_SIZE) //$NON-NLS-1$
		    .build("Chunk " + chunkNum + " has the wrong parent. Expected " + parent //$NON-NLS-1$//$NON-NLS-2$
			    + " but found  " + parentChunk); //$NON-NLS-1$
	}

	visited.add(chunkNum);
	int numChunks = getBlockHeaderForChunkNum(chunkNum);
	for (int testPosition = 0; testPosition &lt; LargeBlock.ENTRIES_IN_CHILD_TABLE; testPosition++) {
	    long nextChildChunkNumAddress = chunkStart + LargeBlock.CHILD_TABLE_OFFSET + (testPosition * INT_SIZE);
	    int nextChildChunkNum = getInt(nextChildChunkNumAddress);

	    if (nextChildChunkNum == 0) {
		continue;
	    }

	    int nextSize = getBlockHeaderForChunkNum(nextChildChunkNum);
	    int sizeDifference = nextSize ^ numChunks;
	    int firstDifference = LargeBlock.SIZE_OF_SIZE_FIELD * 8
		    - Integer.numberOfLeadingZeros(Integer.highestOneBit(sizeDifference)) - 1;

	    if (firstDifference != testPosition) {
		IndexExceptionBuilder descriptor = describeProblem();
		attachBlockHeaderForChunkNum(descriptor, chunkNum);
		attachBlockHeaderForChunkNum(descriptor, nextChildChunkNum);
		throw descriptor.build("Chunk " + nextChildChunkNum + " contained an incorrect size of " //$NON-NLS-1$//$NON-NLS-2$
			+ nextSize + ". It was at position " + testPosition + " in parent " + chunkNum //$NON-NLS-1$ //$NON-NLS-2$
			+ " which had size " + numChunks); //$NON-NLS-1$
	    }

	    try {
		validateFreeSpaceNode(visited, nextChildChunkNum, chunkNum);
	    } catch (IndexException e) {
		describeProblem().addProblemAddress("child pointer from parent " + chunkNum, nextChildChunkNumAddress, //$NON-NLS-1$
			Database.INT_SIZE).attachTo(e);
		throw e;
	    }
	}
    }

    public short getShort(long offset) throws IndexException {
	return getChunk(offset).getShort(offset);
    }

    private void attachBlockHeaderForChunkNum(IndexExceptionBuilder builder, int firstChunkNum) {
	if (firstChunkNum &gt;= this.fChunksUsed) {
	    return;
	}
	builder.addProblemAddress("block header for chunk " + firstChunkNum, ((long) firstChunkNum * CHUNK_SIZE), //$NON-NLS-1$
		Database.INT_SIZE);
    }

}

