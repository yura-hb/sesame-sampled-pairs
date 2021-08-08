import java.util.Arrays;

class SpinedBuffer&lt;E&gt; extends AbstractSpinedBuffer implements Consumer&lt;E&gt;, Iterable&lt;E&gt; {
    /**
     * Force the buffer to increase its capacity.
     */
    protected void increaseCapacity() {
	ensureCapacity(capacity() + 1);
    }

    /**
     * Chunk that we're currently writing into; may or may not be aliased with
     * the first element of the spine.
     */
    protected E[] curChunk;
    /**
     * All chunks, or null if there is only one chunk.
     */
    protected E[][] spine;

    /**
     * Returns the current capacity of the buffer
     */
    protected long capacity() {
	return (spineIndex == 0) ? curChunk.length : priorElementCount[spineIndex] + spine[spineIndex].length;
    }

    /**
     * Ensure that the buffer has at least capacity to hold the target size
     */
    @SuppressWarnings("unchecked")
    protected final void ensureCapacity(long targetSize) {
	long capacity = capacity();
	if (targetSize &gt; capacity) {
	    inflateSpine();
	    for (int i = spineIndex + 1; targetSize &gt; capacity; i++) {
		if (i &gt;= spine.length) {
		    int newSpineSize = spine.length * 2;
		    spine = Arrays.copyOf(spine, newSpineSize);
		    priorElementCount = Arrays.copyOf(priorElementCount, newSpineSize);
		}
		int nextChunkSize = chunkSize(i);
		spine[i] = (E[]) new Object[nextChunkSize];
		priorElementCount[i] = priorElementCount[i - 1] + spine[i - 1].length;
		capacity += nextChunkSize;
	    }
	}
    }

    @SuppressWarnings("unchecked")
    private void inflateSpine() {
	if (spine == null) {
	    spine = (E[][]) new Object[MIN_SPINE_SIZE][];
	    priorElementCount = new long[MIN_SPINE_SIZE];
	    spine[0] = curChunk;
	}
    }

}

