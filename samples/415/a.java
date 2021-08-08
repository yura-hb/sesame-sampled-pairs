class PredicateTransformer&lt;T&gt; implements Transformer&lt;T, Boolean&gt;, Serializable {
    /**
     * Factory method that performs validation.
     *
     * @param &lt;T&gt;  the input type
     * @param predicate  the predicate to call, not null
     * @return the &lt;code&gt;predicate&lt;/code&gt; transformer
     * @throws IllegalArgumentException if the predicate is null
     */
    public static &lt;T&gt; Transformer&lt;T, Boolean&gt; predicateTransformer(final Predicate&lt;? super T&gt; predicate) {
	if (predicate == null) {
	    throw new IllegalArgumentException("Predicate must not be null");
	}
	return new PredicateTransformer&lt;&gt;(predicate);
    }

    /** The closure to wrap */
    private final Predicate&lt;? super T&gt; iPredicate;

    /**
     * Constructor that performs no validation.
     * Use &lt;code&gt;predicateTransformer&lt;/code&gt; if you want that.
     *
     * @param predicate  the predicate to call, not null
     */
    public PredicateTransformer(final Predicate&lt;? super T&gt; predicate) {
	super();
	iPredicate = predicate;
    }

}

