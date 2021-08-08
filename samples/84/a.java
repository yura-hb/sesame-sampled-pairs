import java.time.Duration;
import java.util.concurrent.TimeUnit;

class Caffeine&lt;K, V&gt; {
    /**
    * Specifies that active entries are eligible for automatic refresh once a fixed duration has
    * elapsed after the entry's creation, or the most recent replacement of its value. The semantics
    * of refreshes are specified in {@link LoadingCache#refresh}, and are performed by calling {@link
    * CacheLoader#reload}.
    * &lt;p&gt;
    * Automatic refreshes are performed when the first stale request for an entry occurs. The request
    * triggering refresh will make an asynchronous call to {@link CacheLoader#reload} and immediately
    * return the old value.
    * &lt;p&gt;
    * &lt;b&gt;Note:&lt;/b&gt; &lt;i&gt;all exceptions thrown during refresh will be logged and then swallowed&lt;/i&gt;.
    *
    * @param duration the length of time after an entry is created that it should be considered
    *     stale, and thus eligible for refresh
    * @return this {@code Caffeine} instance (for chaining)
    * @throws IllegalArgumentException if {@code duration} is negative
    * @throws IllegalStateException if the refresh interval was already set
    * @throws ArithmeticException for durations greater than +/- approximately 292 years
    */
    @NonNull
    public Caffeine&lt;K, V&gt; refreshAfterWrite(@NonNull Duration duration) {
	return refreshAfterWrite(duration.toNanos(), TimeUnit.NANOSECONDS);
    }

    long refreshNanos = UNSET_INT;
    static final int UNSET_INT = -1;

    /**
    * Specifies that active entries are eligible for automatic refresh once a fixed duration has
    * elapsed after the entry's creation, or the most recent replacement of its value. The semantics
    * of refreshes are specified in {@link LoadingCache#refresh}, and are performed by calling
    * {@link CacheLoader#reload}.
    * &lt;p&gt;
    * Automatic refreshes are performed when the first stale request for an entry occurs. The request
    * triggering refresh will make an asynchronous call to {@link CacheLoader#reload} and immediately
    * return the old value.
    * &lt;p&gt;
    * &lt;b&gt;Note:&lt;/b&gt; &lt;i&gt;all exceptions thrown during refresh will be logged and then swallowed&lt;/i&gt;.
    * &lt;p&gt;
    * If you can represent the duration as a {@link java.time.Duration} (which should be preferred
    * when feasible), use {@link #refreshAfterWrite(Duration)} instead.
    *
    * @param duration the length of time after an entry is created that it should be considered
    *        stale, and thus eligible for refresh
    * @param unit the unit that {@code duration} is expressed in
    * @return this {@code Caffeine} instance (for chaining)
    * @throws IllegalArgumentException if {@code duration} is zero or negative
    * @throws IllegalStateException if the refresh interval was already set
    */
    @NonNull
    public Caffeine&lt;K, V&gt; refreshAfterWrite(@NonNegative long duration, @NonNull TimeUnit unit) {
	requireNonNull(unit);
	requireState(refreshNanos == UNSET_INT, "refresh was already set to %s ns", refreshNanos);
	requireArgument(duration &gt; 0, "duration must be positive: %s %s", duration, unit);
	this.refreshNanos = unit.toNanos(duration);
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

