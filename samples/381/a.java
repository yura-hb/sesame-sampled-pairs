class Optional&lt;T&gt; {
    /**
     * Returns an Optional describing the specified value, if non-null, otherwise returns an empty Optional.
     *
     * @param value the possibly-null value to describe
     * @return an Optional with a present value if the specified value is non-null, otherwise an empty Optional
     */
    public static &lt;T&gt; Optional&lt;T&gt; ofNullable(T value) {
	if (value == null) {
	    return empty();
	}
	return new Optional&lt;&gt;(value);
    }

    private static final Optional EMPTY = new Optional();
    private final T value;

    /**
     * Returns an empty Optional instance. No value is present for this Optional.
     *
     */
    public static &lt;T&gt; Optional&lt;T&gt; empty() {
	return (Optional&lt;T&gt;) EMPTY;
    }

    private Optional(T value) {
	this.value = value;
    }

}

