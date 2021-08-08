abstract class RateLimiter {
    /**
    * Reserves next ticket and returns the wait time that the caller must wait for.
    *
    * @return the required wait time, never negative
    */
    final long reserveAndGetWaitLength(int permits, long nowMicros) {
	long momentAvailable = reserveEarliestAvailable(permits, nowMicros);
	return max(momentAvailable - nowMicros, 0);
    }

    /**
    * Reserves the requested number of permits and returns the time that those permits can be used
    * (with one caveat).
    *
    * @return the time that the permits may be used, or, if the permits may be used immediately, an
    *     arbitrary past or present time
    */
    abstract long reserveEarliestAvailable(int permits, long nowMicros);

}

