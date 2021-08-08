class Util {
    /**
     * Releases a temporary buffer by returning to the cache or freeing it. If
     * returning to the cache then insert it at the start so that it is
     * likely to be returned by a subsequent call to getTemporaryDirectBuffer.
     */
    static void offerFirstTemporaryDirectBuffer(ByteBuffer buf) {
	// If the buffer is too large for the cache we don't have to
	// check the cache. We'll just free it.
	if (isBufferTooLarge(buf)) {
	    free(buf);
	    return;
	}

	assert buf != null;
	BufferCache cache = bufferCache.get();
	if (!cache.offerFirst(buf)) {
	    // cache is full
	    free(buf);
	}
    }

    private static ThreadLocal&lt;BufferCache&gt; bufferCache = new TerminatingThreadLocal&lt;&gt;() {
	@Override
	protected BufferCache initialValue() {
	    return new BufferCache();
	}

	@Override
	protected void threadTerminated(BufferCache cache) { // will never be null
	    while (!cache.isEmpty()) {
		ByteBuffer bb = cache.removeFirst();
		free(bb);
	    }
	}
    };
    private static final int TEMP_BUF_POOL_SIZE = IOUtil.IOV_MAX;
    private static final long MAX_CACHED_BUFFER_SIZE = getMaxCachedBufferSize();

    /**
     * Returns true if the buffer is too large to be added to the
     * buffer cache, false otherwise.
     */
    private static boolean isBufferTooLarge(ByteBuffer buf) {
	return isBufferTooLarge(buf.capacity());
    }

    /**
     * Frees the memory for the given direct buffer
     */
    private static void free(ByteBuffer buf) {
	((DirectBuffer) buf).cleaner().clean();
    }

    /**
     * Returns true if a buffer of this size is too large to be
     * added to the buffer cache, false otherwise.
     */
    private static boolean isBufferTooLarge(int size) {
	return size &gt; MAX_CACHED_BUFFER_SIZE;
    }

    class BufferCache {
	private static ThreadLocal&lt;BufferCache&gt; bufferCache = new TerminatingThreadLocal&lt;&gt;() {
	    @Override
	    protected BufferCache initialValue() {
		return new BufferCache();
	    }

	    @Override
	    protected void threadTerminated(BufferCache cache) { // will never be null
		while (!cache.isEmpty()) {
		    ByteBuffer bb = cache.removeFirst();
		    free(bb);
		}
	    }
	};
	private static final int TEMP_BUF_POOL_SIZE = IOUtil.IOV_MAX;
	private static final long MAX_CACHED_BUFFER_SIZE = getMaxCachedBufferSize();

	boolean offerFirst(ByteBuffer buf) {
	    // Don't call this if the buffer is too large.
	    assert !isBufferTooLarge(buf);

	    if (count &gt;= TEMP_BUF_POOL_SIZE) {
		return false;
	    } else {
		start = (start + TEMP_BUF_POOL_SIZE - 1) % TEMP_BUF_POOL_SIZE;
		buffers[start] = buf;
		count++;
		return true;
	    }
	}

    }

}

