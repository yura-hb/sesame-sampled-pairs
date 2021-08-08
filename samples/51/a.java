abstract class BoundedLocalCache&lt;K, V&gt; extends DrainStatusRef&lt;K, V&gt; implements LocalCache&lt;K, V&gt; {
    /** Returns if the entry has expired. */
    @SuppressWarnings("ShortCircuitBoolean")
    boolean hasExpired(Node&lt;K, V&gt; node, long now) {
	return (expiresAfterAccess() && (now - node.getAccessTime() &gt;= expiresAfterAccessNanos()))
		| (expiresAfterWrite() && (now - node.getWriteTime() &gt;= expiresAfterWriteNanos()))
		| (expiresVariable() && (now - node.getVariableTime() &gt;= 0));
    }

    /** Returns if the cache expires entries after an access time threshold. */
    protected boolean expiresAfterAccess() {
	return false;
    }

    /** Returns how long after the last access to an entry the map will retain that entry. */
    protected long expiresAfterAccessNanos() {
	throw new UnsupportedOperationException();
    }

    /** Returns if the cache expires entries after an write time threshold. */
    protected boolean expiresAfterWrite() {
	return false;
    }

    /** Returns how long after the last write to an entry the map will retain that entry. */
    protected long expiresAfterWriteNanos() {
	throw new UnsupportedOperationException();
    }

    /** Returns if the cache expires entries after a variable time threshold. */
    protected boolean expiresVariable() {
	return false;
    }

}

