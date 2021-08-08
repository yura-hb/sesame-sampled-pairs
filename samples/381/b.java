class Optional&lt;T&gt; {
    /**
     * Returns an {@code Optional} describing the given value, if
     * non-{@code null}, otherwise returns an empty {@code Optional}.
     *
     * @param value the possibly-{@code null} value to describe
     * @param &lt;T&gt; the type of the value
     * @return an {@code Optional} with a present value if the specified value
     *         is non-{@code null}, otherwise an empty {@code Optional}
     */
    public static &lt;T&gt; Optional&lt;T&gt; ofNullable(T value) {
	return value == null ? empty() : of(value);
    }

    /**
     * Common instance for {@code empty()}.
     */
    private static final Optional&lt;?&gt; EMPTY = new Optional&lt;&gt;();
    /**
     * If non-null, the value; if null, indicates no value is present
     */
    private final T value;

    /**
     * Returns an empty {@code Optional} instance.  No value is present for this
     * {@code Optional}.
     *
     * @apiNote
     * Though it may be tempting to do so, avoid testing if an object is empty
     * by comparing with {@code ==} against instances returned by
     * {@code Optional.empty()}.  There is no guarantee that it is a singleton.
     * Instead, use {@link #isPresent()}.
     *
     * @param &lt;T&gt; The type of the non-existent value
     * @return an empty {@code Optional}
     */
    public static &lt;T&gt; Optional&lt;T&gt; empty() {
	@SuppressWarnings("unchecked")
	Optional&lt;T&gt; t = (Optional&lt;T&gt;) EMPTY;
	return t;
    }

    /**
     * Returns an {@code Optional} describing the given non-{@code null}
     * value.
     *
     * @param value the value to describe, which must be non-{@code null}
     * @param &lt;T&gt; the type of the value
     * @return an {@code Optional} with the value present
     * @throws NullPointerException if value is {@code null}
     */
    public static &lt;T&gt; Optional&lt;T&gt; of(T value) {
	return new Optional&lt;&gt;(value);
    }

    /**
     * Constructs an instance with the described value.
     *
     * @param value the non-{@code null} value to describe
     * @throws NullPointerException if value is {@code null}
     */
    private Optional(T value) {
	this.value = Objects.requireNonNull(value);
    }

}

