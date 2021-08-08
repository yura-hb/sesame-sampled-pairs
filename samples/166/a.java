class CacheWriterVerifier {
    /** Checks that the expected number of delete operations occurred. */
    public void deletions(long count, RemovalCause cause) {
	verify(context.cacheWriter(), times((int) count)).delete(any(), any(), eq(cause));
    }

    private final CacheContext context;

}

