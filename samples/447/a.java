class Caffeine&lt;K, V&gt; {
    /**
    * Specifies a nanosecond-precision time source for use in determining when entries should be
    * expired or refreshed. By default, {@link System#nanoTime} is used.
    * &lt;p&gt;
    * The primary intent of this method is to facilitate testing of caches which have been configured
    * with {@link #expireAfterWrite}, {@link #expireAfterAccess}, or {@link #refreshAfterWrite}.
    *
    * @param ticker a nanosecond-precision time source
    * @return this {@code Caffeine} instance (for chaining)
    * @throws IllegalStateException if a ticker was already set
    * @throws NullPointerException if the specified ticker is null
    */
    @NonNull
    public Caffeine&lt;K, V&gt; ticker(@NonNull Ticker ticker) {
	requireState(this.ticker == null, "Ticker was already set to %s", this.ticker);
	this.ticker = requireNonNull(ticker);
	return this;
    }

    @Nullable
    Ticker ticker;

    /** Ensures that the state expression is true. */
    static void requireState(boolean expression, String template, Object... args) {
	if (!expression) {
	    throw new IllegalStateException(String.format(template, args));
	}
    }

}

