import java.time.Duration;
import java.util.concurrent.TimeUnit;

class Caffeine&lt;K, V&gt; {
    /**
    * Specifies that each entry should be automatically removed from the cache once a fixed duration
    * has elapsed after the entry's creation, the most recent replacement of its value, or its last
    * access. Access time is reset by all cache read and write operations (including {@code
    * Cache.asMap().get(Object)} and {@code Cache.asMap().put(K, V)}), but not by operations on the
    * collection-views of {@link Cache#asMap}.
    * &lt;p&gt;
    * Expired entries may be counted in {@link Cache#estimatedSize()}, but will never be visible to
    * read or write operations. Expired entries are cleaned up as part of the routine maintenance
    * described in the class javadoc.
    *
    * @param duration the length of time after an entry is last accessed that it should be
    *        automatically removed
    * @return this {@code Caffeine} instance (for chaining)
    * @throws IllegalArgumentException if {@code duration} is negative
    * @throws IllegalStateException if the time to idle or time to live was already set
    * @throws ArithmeticException for durations greater than +/- approximately 292 years
    */
    @NonNull
    public Caffeine&lt;K, V&gt; expireAfterAccess(@NonNull Duration duration) {
	return expireAfterAccess(duration.toNanos(), TimeUnit.NANOSECONDS);
    }

    long expireAfterAccessNanos = UNSET_INT;
    static final int UNSET_INT = -1;
    @Nullable
    Expiry&lt;? super K, ? super V&gt; expiry;

    /**
    * Specifies that each entry should be automatically removed from the cache once a fixed duration
    * has elapsed after the entry's creation, the most recent replacement of its value, or its last
    * read. Access time is reset by all cache read and write operations (including
    * {@code Cache.asMap().get(Object)} and {@code Cache.asMap().put(K, V)}), but not by operations
    * on the collection-views of {@link Cache#asMap}.
    * &lt;p&gt;
    * Expired entries may be counted in {@link Cache#estimatedSize()}, but will never be visible to
    * read or write operations. Expired entries are cleaned up as part of the routine maintenance
    * described in the class javadoc.
    * &lt;p&gt;
    * If you can represent the duration as a {@link java.time.Duration} (which should be preferred
    * when feasible), use {@link #expireAfterAccess(Duration)} instead.
    *
    * @param duration the length of time after an entry is last accessed that it should be
    *        automatically removed
    * @param unit the unit that {@code duration} is expressed in
    * @return this {@code Caffeine} instance (for chaining)
    * @throws IllegalArgumentException if {@code duration} is negative
    * @throws IllegalStateException if the time to idle or variable expiration was already set
    */
    @NonNull
    public Caffeine&lt;K, V&gt; expireAfterAccess(@NonNegative long duration, @NonNull TimeUnit unit) {
	requireState(expireAfterAccessNanos == UNSET_INT, "expireAfterAccess was already set to %s ns",
		expireAfterAccessNanos);
	requireState(expiry == null, "expireAfterAccess may not be used with variable expiration");
	requireArgument(duration &gt;= 0, "duration cannot be negative: %s %s", duration, unit);
	this.expireAfterAccessNanos = unit.toNanos(duration);
	return this;
    }

    /** Ensures that the state expression is true. */
    static void requireState(boolean expression, String template, Object... args) {
	if (!expression) {
	    throw new IllegalStateException(String.format(template, args));
	}
    }

    /** Ensures that the argument expression is true. */
    static void requireArgument(boolean expression, String template, Object... args) {
	if (!expression) {
	    throw new IllegalArgumentException(String.format(template, args));
	}
    }

}

