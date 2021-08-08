class LocalCache&lt;K, V&gt; extends AbstractMap&lt;K, V&gt; implements ConcurrentMap&lt;K, V&gt; {
    /** Returns true if the entry has expired. */
    boolean isExpired(ReferenceEntry&lt;K, V&gt; entry, long now) {
	checkNotNull(entry);
	if (expiresAfterAccess() && (now - entry.getAccessTime() &gt;= expireAfterAccessNanos)) {
	    return true;
	}
	if (expiresAfterWrite() && (now - entry.getWriteTime() &gt;= expireAfterWriteNanos)) {
	    return true;
	}
	return false;
    }

    /** How long after the last access to an entry the map will retain that entry. */
    final long expireAfterAccessNanos;
    /** How long after the last write to an entry the map will retain that entry. */
    final long expireAfterWriteNanos;

    boolean expiresAfterAccess() {
	return expireAfterAccessNanos &gt; 0;
    }

    boolean expiresAfterWrite() {
	return expireAfterWriteNanos &gt; 0;
    }

}

