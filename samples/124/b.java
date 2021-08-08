import java.util.concurrent.TimeUnit;

class CacheBuilder&lt;K, V&gt; {
    /**
    * Specifies that each entry should be automatically removed from the cache once a fixed duration
    * has elapsed after the entry's creation, the most recent replacement of its value, or its last
    * access. Access time is reset by all cache read and write operations (including {@code
    * Cache.asMap().get(Object)} and {@code Cache.asMap().put(K, V)}), but not by operations on the
    * collection-views of {@link Cache#asMap}.
    *
    * &lt;p&gt;When {@code duration} is zero, this method hands off to {@link #maximumSize(long)
    * maximumSize}{@code (0)}, ignoring any otherwise-specified maximum size or weight. This can be
    * useful in testing, or to disable caching temporarily without a code change.
    *
    * &lt;p&gt;Expired entries may be counted in {@link Cache#size}, but will never be visible to read or
    * write operations. Expired entries are cleaned up as part of the routine maintenance described
    * in the class javadoc.
    *
    * &lt;p&gt;If you can represent the duration as a {@link java.time.Duration} (which should be preferred
    * when feasible), use {@link #expireAfterAccess(Duration)} instead.
    *
    * @param duration the length of time after an entry is last accessed that it should be
    *     automatically removed
    * @param unit the unit that {@code duration} is expressed in
    * @return this {@code CacheBuilder} instance (for chaining)
    * @throws IllegalArgumentException if {@code duration} is negative
    * @throws IllegalStateException if the time to idle or time to live was already set
    */
    public CacheBuilder&lt;K, V&gt; expireAfterAccess(long duration, TimeUnit unit) {
	checkState(expireAfterAccessNanos == UNSET_INT, "expireAfterAccess was already set to %s ns",
		expireAfterAccessNanos);
	checkArgument(duration &gt;= 0, "duration cannot be negative: %s %s", duration, unit);
	this.expireAfterAccessNanos = unit.toNanos(duration);
	return this;
    }

    long expireAfterAccessNanos = UNSET_INT;
    static final int UNSET_INT = -1;

}

