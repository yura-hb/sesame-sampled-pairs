import java.util.concurrent.TimeUnit;

class CacheBuilder&lt;K, V&gt; {
    /**
    * Specifies that active entries are eligible for automatic refresh once a fixed duration has
    * elapsed after the entry's creation, or the most recent replacement of its value. The semantics
    * of refreshes are specified in {@link LoadingCache#refresh}, and are performed by calling {@link
    * CacheLoader#reload}.
    *
    * &lt;p&gt;As the default implementation of {@link CacheLoader#reload} is synchronous, it is
    * recommended that users of this method override {@link CacheLoader#reload} with an asynchronous
    * implementation; otherwise refreshes will be performed during unrelated cache read and write
    * operations.
    *
    * &lt;p&gt;Currently automatic refreshes are performed when the first stale request for an entry
    * occurs. The request triggering refresh will make a blocking call to {@link CacheLoader#reload}
    * and immediately return the new value if the returned future is complete, and the old value
    * otherwise.
    *
    * &lt;p&gt;&lt;b&gt;Note:&lt;/b&gt; &lt;i&gt;all exceptions thrown during refresh will be logged and then swallowed&lt;/i&gt;.
    *
    * &lt;p&gt;If you can represent the duration as a {@link java.time.Duration} (which should be preferred
    * when feasible), use {@link #refreshAfterWrite(Duration)} instead.
    *
    * @param duration the length of time after an entry is created that it should be considered
    *     stale, and thus eligible for refresh
    * @param unit the unit that {@code duration} is expressed in
    * @return this {@code CacheBuilder} instance (for chaining)
    * @throws IllegalArgumentException if {@code duration} is negative
    * @throws IllegalStateException if the refresh interval was already set
    * @since 11.0
    */
    @GwtIncompatible // To be supported (synchronously).
    public CacheBuilder&lt;K, V&gt; refreshAfterWrite(long duration, TimeUnit unit) {
	checkNotNull(unit);
	checkState(refreshNanos == UNSET_INT, "refresh was already set to %s ns", refreshNanos);
	checkArgument(duration &gt; 0, "duration must be positive: %s %s", duration, unit);
	this.refreshNanos = unit.toNanos(duration);
	return this;
    }

    long refreshNanos = UNSET_INT;
    static final int UNSET_INT = -1;

}

