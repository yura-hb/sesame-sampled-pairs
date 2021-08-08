class Pair&lt;L, R&gt; {
    /**
     * Returns an empty pair.
     *
     * @since 1.0
     */
    @SuppressWarnings("unchecked")
    public static &lt;L, R&gt; Pair&lt;L, R&gt; empty() {
	return (Pair&lt;L, R&gt;) EMPTY;
    }

    private static final Pair&lt;Object, Object&gt; EMPTY = new Pair&lt;&gt;(null, null);

}

