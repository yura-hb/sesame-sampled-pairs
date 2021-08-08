import org.eclipse.jdt.internal.core.nd.db.Database;

class NdRawLinkedList {
    /**
     * Adds a new element to the list and returns the record pointer to the start of the newly-allocated object
     *
     * @param metadataBits the metadata bits to attach to the new member. Use 0 if this list does not use metadata.
     */
    public long addMember(short metadataBits) throws IndexException {
	Database db = getDB();
	long current = this.lastKnownBlock;
	int thisBlockRecordCount = this.firstBlockRecordCount;
	while (true) {
	    long ptr = db.getRecPtr(current + NEXT_MEMBER_BLOCK);
	    int elementsInBlock = getElementsInBlock(current, ptr, thisBlockRecordCount);

	    // If there's room in this block
	    if (elementsInBlock &lt; thisBlockRecordCount) {
		long positionOfElementCount = getAddressOfElement(current, thisBlockRecordCount - 1);
		// If there's only one space left
		if (elementsInBlock == thisBlockRecordCount - 1) {
		    // We use the fact that the next pointer points to itself as a sentinel to indicate that the
		    // block is full and there are no further blocks
		    db.putRecPtr(current + NEXT_MEMBER_BLOCK, current);
		    // Zero out the int we've been using to hold the count of elements
		    db.putInt(positionOfElementCount, 0);
		} else {
		    // Increment the element count
		    db.putInt(positionOfElementCount, elementsInBlock + 1);
		}

		if (this.metadataBitsPerRecord &gt; 0) {
		    int metadataMask = (1 &lt;&lt; this.metadataBitsPerRecord) - 1;
		    int metadataRecordsPerShort = this.metadataBitsPerRecord == 0 ? 0
			    : (16 / this.metadataBitsPerRecord);
		    metadataBits &= metadataMask;

		    int metadataBitOffset = elementsInBlock % metadataRecordsPerShort;
		    long metadataStart = getAddressOfMetadata(current, thisBlockRecordCount);
		    int whichShort = elementsInBlock / metadataRecordsPerShort;
		    long metadataOffset = metadataStart + 2 * whichShort;
		    short metadataValue = db.getShort(metadataOffset);

		    // Resetting the previous visibility bits of the target member.
		    metadataValue &= ~(metadataMask &lt;&lt; metadataBitOffset * this.metadataBitsPerRecord);
		    // Setting the new visibility bits of the target member.
		    metadataValue |= metadataBits &lt;&lt; metadataBitOffset * this.metadataBitsPerRecord;

		    getDB().putShort(metadataOffset, metadataValue);
		}

		this.lastKnownBlock = current;
		return getAddressOfElement(current, elementsInBlock);
	    } else {
		// When ptr == current, this is a sentinel indicating that the block is full and there are no
		// further blocks. If this is the case, create a new block
		if (isLastBlock(current, ptr)) {
		    current = db.malloc(
			    recordSize(this.elementRecordSize, this.recordCount, this.metadataBitsPerRecord),
			    Database.POOL_LINKED_LIST);
		    db.putRecPtr(current + NEXT_MEMBER_BLOCK, current);
		} else {
		    thisBlockRecordCount = this.recordCount;
		    // Else, there are more blocks following this one so advance
		    current = ptr;
		}
	    }
	}
    }

    private long lastKnownBlock;
    private final int firstBlockRecordCount;
    private static final int NEXT_MEMBER_BLOCK = 0;
    private final int metadataBitsPerRecord;
    private final int elementRecordSize;
    private final int recordCount;
    private final Nd nd;
    private static final int ELEMENT_START_POSITION = NEXT_MEMBER_BLOCK + Database.PTR_SIZE;

    private Database getDB() {
	return this.nd.getDB();
    }

    private int getElementsInBlock(long currentRecord, long ptr, int currentRecordCount) throws IndexException {
	if (ptr == 0 && currentRecordCount &gt; 0) {
	    return getDB().getInt(getAddressOfElement(currentRecord, currentRecordCount - 1));
	}
	return currentRecordCount;
    }

    private long getAddressOfElement(long blockRecordStart, int elementNumber) {
	return blockRecordStart + ELEMENT_START_POSITION + elementNumber * this.elementRecordSize;
    }

    private long getAddressOfMetadata(long blockRecordStart, int blockRecordCount) {
	return getAddressOfElement(blockRecordStart, blockRecordCount);
    }

    private boolean isLastBlock(long blockAddress, long pointerToNextBlock) {
	return pointerToNextBlock == 0 || pointerToNextBlock == blockAddress;
    }

    /**
     * Returns the record size for a linked list with the given element record size and number of
     * records per block
     */
    public static int recordSize(int elementRecordSize, int recordsPerBlock, int metadataBitsPerRecord) {
	int metadataSize = 0;

	if (metadataBitsPerRecord &gt; 0) {
	    int metadataRecordsPerShort = 16 / metadataBitsPerRecord;
	    int numberOfShorts = (recordsPerBlock + metadataRecordsPerShort - 1) / metadataRecordsPerShort;

	    metadataSize = 2 * numberOfShorts;
	}

	return Database.PTR_SIZE + elementRecordSize * recordsPerBlock + metadataSize;
    }

}

